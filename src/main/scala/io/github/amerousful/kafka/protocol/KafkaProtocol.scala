package io.github.amerousful.kafka.protocol

import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import org.apache.kafka.clients.producer.KafkaProducer
import io.github.amerousful.kafka.client.KafkaTrackerPoll

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._


object KafkaProtocol extends StrictLogging {
  val kafkaProtocolKey: ProtocolKey[KafkaProtocol, KafkaComponents] = new ProtocolKey[KafkaProtocol, KafkaComponents] {
    override def protocolClass: Class[io.gatling.core.protocol.Protocol] =
      classOf[KafkaProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): KafkaProtocol =
      throw new IllegalStateException("Can't provide a default value for KafkaProtocol")

    override def newComponents(coreComponents: CoreComponents): KafkaProtocol => KafkaComponents = {
      kafkaProtocol => {

        val producerProperties = kafkaProtocol.producerProperties.asJava
        val consumerProperties: Map[String, AnyRef] = kafkaProtocol.consumerProperties

        if (logger.underlying.isDebugEnabled()) {
          val prodProps = kafkaProtocol.producerProperties.map(i => s"${i._1}: ${i._2}").mkString("\n")
          logger.debug(s"Producer properties:\n$prodProps\n")
        }

        val producer = new KafkaProducer[String, String](producerProperties)
        val trackerPoller = new KafkaTrackerPoll(
          consumerProperties,
          coreComponents.actorSystem,
          coreComponents.statsEngine,
          coreComponents.clock)

        coreComponents.actorSystem.registerOnTermination {
          logger.debug("Termination was registered. Producer and poller gonna close")
          producer.close()
          trackerPoller.close()
        }

        KafkaComponents(kafkaProtocol, producer, trackerPoller)
      }
    }
  }

  def apply(): KafkaProtocol = KafkaProtocol(
    producerProperties = Map.empty,
    consumerProperties = Map.empty,
    messageMatcher = KafkaKeyMatcher,
    replyTimeout = None
  )
}

final case class KafkaProtocol(
                                producerProperties: Map[String, AnyRef],
                                consumerProperties: Map[String, AnyRef],
                                messageMatcher: KafkaMatcher,
                                replyTimeout: Option[FiniteDuration]
                              ) extends Protocol {
  type Components = KafkaComponents
}
