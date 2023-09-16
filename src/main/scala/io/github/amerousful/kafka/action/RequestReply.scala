package io.github.amerousful.kafka.action

import io.gatling.commons.stats.KO
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.github.amerousful.kafka.client.KafkaTrackerPoll
import io.github.amerousful.kafka.protocol.KafkaProtocol
import io.github.amerousful.kafka.request.KafkaAttributes
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class RequestReply(
                    attributes: KafkaAttributes,
                    replyTopic: Expression[String],
                    protocol: KafkaProtocol,
                    producer: KafkaProducer[String, String],
                    kafkaTrackerPoll: KafkaTrackerPoll,
                    val statsEngine: StatsEngine,
                    val clock: Clock,
                    val next: Action,
                  ) extends KafkaAction(attributes, protocol, producer, kafkaTrackerPoll) {

  override val name: String = genName("kafkaRequestReply")
  private val messageMatcher = protocol.messageMatcher
  private val replyTimeoutInMs = protocol.replyTimeout.fold(0L)(_.toMillis)

  override protected def aroundSend(requestName: String, session: Session, producerRecord: ProducerRecord[String, String], topic: String): Validation[Around] =
    for {
      resolvedReadTopic <- replyTopic(session)
    } yield {


      val matchId: String = messageMatcher.requestMatchId(producerRecord)
      val tracker = kafkaTrackerPoll.tracker(resolvedReadTopic, messageMatcher)

      new Around(
        before = () => {
          if (logger.underlying.isDebugEnabled) {
            logger.debug(s"Sent Kafka message. Topic: $topic Key: ${producerRecord.key()} Payload: ${producerRecord.value()}")
          }

          if (matchId != null) {
            tracker.track(matchId, clock.nowMillis, replyTimeoutInMs, attributes.checks, session, next, requestName)
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
