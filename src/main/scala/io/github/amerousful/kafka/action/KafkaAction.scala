package io.github.amerousful.kafka.action

import io.gatling.commons.validation.{SuccessWrapper, Validation}
import io.gatling.core.action.RequestAction
import io.gatling.core.session.{Expression, Session, resolveOptionalExpression}
import io.gatling.core.util.NameGen
import io.github.amerousful.kafka.client.KafkaTrackerPoll
import io.github.amerousful.kafka.protocol.KafkaProtocol
import io.github.amerousful.kafka.request.KafkaAttributes
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders


class Around(before: () => Unit, after: () => Unit) {
  def apply(f: => Any): Unit = {
    before()
    f
    after()
  }
}

abstract class KafkaAction(
                            attributes: KafkaAttributes,
                            protocol: KafkaProtocol,
                            producer: KafkaProducer[String, Any],
                            kafkaTrackerPoll: KafkaTrackerPoll
                          ) extends RequestAction with KafkaLogging with NameGen {

  override val requestName: Expression[String] = attributes.requestName

  override def sendRequest(session: Session): Validation[Unit] = {
    for {
      reqName <- requestName(session)
      topic <- attributes.topic(session)
      key <- resolveOptionalExpression(attributes.key, session)
      payload <- attributes.payload(session)
      headers <- resolveHeaders(attributes.headers, session)
      record <- new ProducerRecord(topic, null, key.getOrElse(""), payload, headers).success
      around <- aroundSend(reqName, session, record, topic)
    } yield {
      if (!attributes.onlyConsume) around(producer.send(record))
      else around(None)
    }
  }

  private def resolveHeaders(headers: Map[Expression[String], Expression[String]], session: Session): Validation[Headers] =
    headers.foldLeft(new RecordHeaders().success) {
      case (resolvedHeaders, (key, value)) =>
        for {
          key <- key(session)
          value <- value(session)
          resolvedHeaders <- resolvedHeaders
        } yield {
          resolvedHeaders.add(key, value.getBytes)
          resolvedHeaders
        }
    }

  protected def aroundSend(requestName: String, session: Session, producerRecord: ProducerRecord[String, Any], topic: String): Validation[Around]

}
