package io.github.amerousful.kafka.action

import io.gatling.commons.stats.KO
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import io.github.amerousful.kafka.client.KafkaTrackerPoll
import io.github.amerousful.kafka.protocol.KafkaProtocol
import io.github.amerousful.kafka.request.KafkaAttributes

class OnlyConsume(
                   attributes: KafkaAttributes,
                   readTopic: Expression[String],
                   protocol: KafkaProtocol,
                   producer: KafkaProducer[String, Any],
                   kafkaTrackerPoll: KafkaTrackerPoll,
                   val statsEngine: StatsEngine,
                   val clock: Clock,
                   val next: Action,
                 ) extends KafkaAction(attributes, protocol, producer, kafkaTrackerPoll) {

  override val name: String = genName("kafkaOnlyConsume")
  private val messageMatcher = protocol.messageMatcher
  private val replyTimeoutInMs = protocol.replyTimeout.fold(0L)(_.toMillis)

  override protected def aroundSend(requestName: String, session: Session, producerRecord: ProducerRecord[String, Any], topic: String): Validation[Around] = {
    for {
      resolvedReadTopic <- readTopic(session)
      startTime <- attributes.startTime(session)
    } yield {

      val matchId: Any = messageMatcher.requestMatchId(producerRecord)
      val tracker = kafkaTrackerPoll.tracker(resolvedReadTopic, messageMatcher, attributes)

      new Around(
        before = () => {
          if (logger.underlying.isDebugEnabled) {
            logger.debug(s"ONLY FOR TRACKING! Kafka message. Topic: $topic Key: ${producerRecord.key()} Payload: ${producerRecord.value()}")
          }

          val time: Long =
            if (startTime == 0L) {
              logger.debug("Start time wasn't set. Current time will be used.")
              clock.nowMillis
            } else {
              logger.debug(s"Specified time is taken: $startTime")
              startTime
            }

          if (matchId != null) {
            tracker.track(matchId, time, replyTimeoutInMs, attributes.checks, session, next, requestName)
          }
        },
        after = () => {
          if (matchId == null) {
            val updatedMatchId = messageMatcher.requestMatchId(producerRecord)

            if (updatedMatchId != null) {
              tracker.track(updatedMatchId, clock.nowMillis, replyTimeoutInMs, attributes.checks, session, next, requestName)
            } else {
              val now = clock.nowMillis
              statsEngine.logResponse(session.scenario, session.groups, requestName, now, now, KO, None, Some("Failed to get a matchId to track"))
              next ! session.markAsFailed
            }
          }
        }
      )
    }
  }
}
