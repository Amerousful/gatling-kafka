package io.github.amerousful.kafka.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.{SuccessWrapper, Validation}
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.github.amerousful.kafka.protocol.KafkaProtocol
import io.github.amerousful.kafka.request.KafkaAttributes
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class Send(
            attributes: KafkaAttributes,
            protocol: KafkaProtocol,
            producer: KafkaProducer[String, Any],
            val statsEngine: StatsEngine,
            val clock: Clock,
            val next: Action,
          ) extends KafkaAction(attributes, protocol, producer, null) {
  override val name: String = genName("kafkaSend")

  override protected def aroundSend(requestName: String, session: Session, producerRecord: ProducerRecord[String, Any], topic: String): Validation[Around] = {
    new Around(
      before = () => {

        if (logger.underlying.isDebugEnabled) {
          logger.debug(s"Sent Kafka message. Topic: $topic Key: ${producerRecord.key()} Payload: ${producerRecord.value()}")
        }

        val now = clock.nowMillis
        statsEngine.logResponse(session.scenario, session.groups, requestName, now, now, OK, None, None)
        next ! session
      },
      after = () => ()
    ).success
  }
}
