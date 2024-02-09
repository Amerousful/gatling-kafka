package io.github.amerousful.kafka.client

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.commons.util.Clock
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import io.github.amerousful.kafka.action.KafkaLogging
import io.github.amerousful.kafka.protocol.KafkaMatcher
import org.apache.kafka.common.serialization.StringDeserializer

import java.util.concurrent.{ConcurrentHashMap, CountDownLatch}
import scala.concurrent.ExecutionContext.Implicits.global

private final case class TrackerAndController(kafkaTracker: KafkaTracker, consumerControl: Consumer.Control)

class KafkaTrackerPoll(
                        consumerProperties: Map[String, AnyRef],
                        system: ActorSystem,
                        statsEngine: StatsEngine,
                        clock: Clock,
                      ) extends KafkaLogging with NameGen {

  private val trackers = new ConcurrentHashMap[String, TrackerAndController]

  private val rebalancingLatch: CountDownLatch = new CountDownLatch(1)

  // Disable logs for Actor
  private val disableLogsConfig: Config = {
    val configString =
      """
        | akka {
        |   loglevel = "OFF"
        | }
        """.stripMargin

    val config = ConfigFactory.parseString(configString)
    ConfigFactory.load(config)
  }
  implicit lazy val systemAkkaConsumer: ActorSystem = ActorSystem("KafkaAkkaConsumer", disableLogsConfig)
  implicit lazy val materializer: Materializer = Materializer.matFromSystem(systemAkkaConsumer)

  def close(): Unit = trackers.values().forEach {
    case TrackerAndController(_, consumerControl) => consumerControl.shutdown()
  }

  private def dealWithConsumerProperties(): Map[String, String] = {
    val properties: Map[String, AnyRef] = (consumerProperties ++ Map(
      "enable.auto.commit" -> "true",
      "auto.offset.reset" -> "latest"
    )).updatedWith("group.id")({
      case None => Some(s"gatling-test-${java.util.UUID.randomUUID()}")
      case Some(value) => Some(value)
    })

    val updateProperties: Map[String, String] = properties.map {
      case (key, value: String) => key -> value
      case (key, value) => key -> value.toString
    }

    val consProps = updateProperties.map(i => s"${i._1}: ${i._2}").mkString("\n")
    logger.debug(s"Consumer properties:\n$consProps\n")

    updateProperties
  }

  private def createConsumer(readTopic: String) = {
    val properties = dealWithConsumerProperties()

    val consumerName: String = properties("group.id")
    logger.debug(s"Create consumer - $consumerName")

    val kafkaConfig = systemAkkaConsumer.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings =
      ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
        .withProperties(properties)

    val subscription = Subscriptions
      .topics(readTopic)
      .withPartitionAssignmentHandler(WaitRebalancing(consumerName, () => rebalancingLatch.countDown()))

    Consumer.plainSource(consumerSettings, subscription)
  }

  def tracker(readTopic: String, messageMatcher: KafkaMatcher): KafkaTracker = {
    trackers.computeIfAbsent(
      readTopic,
      _ => {
        val actor = system.actorOf(Tracker.props(statsEngine, clock), genName("kafkaTrackerActor"))
        val consumer = createConsumer(readTopic)

        val (consumerControl, streamComplete) =
          consumer
            .toMat(Sink.foreach { record =>
              val matchId = messageMatcher.responseMatchId(record)
              logger.debug(s"Received Kafka message. Key: ${record.key()} Payload: ${record.value()}. With matchId - $matchId")
              actor ! MessageReceived(matchId, clock.nowMillis, record)

            })(Keep.both)
            .run()

        streamComplete.onComplete { _ =>
          logger.debug("Kafka akka consumer stream has been completed.")
          systemAkkaConsumer.terminate()
        }

        // We cannot yield tracker until partitions aren't rebalanced for consumer.
        // Rebalancing takes a while.
        rebalancingLatch.await()

        TrackerAndController(new KafkaTracker(actor), consumerControl)
      }
    ).kafkaTracker
  }
}

