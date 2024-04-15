package io.github.amerousful.kafka.request

import com.softwaremill.quicklens.ModifyPimp
import io.gatling.commons.validation.Validation
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.el.El
import io.gatling.core.session.{Expression, Session}
import io.github.amerousful.kafka.action.{OnlyConsumeBuilder, RequestReplyBuilder, SendBuilder}
import io.github.amerousful.kafka.client.SerdeScalaPB
import io.github.amerousful.kafka.client.SerdeScalaPB.ProtoBufScalaPB
import io.github.amerousful.kafka.{KafkaCheck, KafkaResponseMessage}
import scalapb.GeneratedMessage

final class KafkaDslBuilderBase(requestName: Expression[String]) {
  def send: SendDslBuilder.Topic = SendDslBuilder.Topic(requestName)

  def requestReply: RequestReplyDslBuilder.Topic = RequestReplyDslBuilder.Topic(requestName)

  def onlyConsume: OnlyConsumeDslBuilder.Topic = OnlyConsumeDslBuilder.Topic(requestName)
}

object SendDslBuilder {
  final case class Topic(requestName: Expression[String]) {
    def topic(topicName: Expression[String]): Payload = Payload(requestName, topicName)
  }

  final case class Payload(requestName: Expression[String], topicName: Expression[String]) {
    def payload(payload: Expression[Any]) = SendDslBuilder(
      KafkaAttributes(requestName, topicName, payload),
      new SendBuilder(_))
  }

}

final case class SendDslBuilder(attributes: KafkaAttributes, factory: KafkaAttributes => ActionBuilder) {

  def key(key: Expression[String]) = this.modify(_.attributes.key).setTo(Some(key))

  def header(name: Expression[String], value: Expression[String]) =
    this.modify(_.attributes.headers)(_ + (name -> value))

  def headers(newHeaders: Map[String, String]) =
    this.modify(_.attributes.headers)(_ ++ newHeaders.map { v => v._1.el[String] -> v._2.el[String] })

  def build: ActionBuilder = factory(attributes)
}


object RequestReplyDslBuilder {
  final case class Topic(requestName: Expression[String]) {
    def topic(topicName: Expression[String]): Payload = Payload(requestName, topicName)
  }

  final case class Payload(requestName: Expression[String], topicName: Expression[String]) {
    def payload(payload: Expression[Any]): ReplyTopic = ReplyTopic(requestName, topicName, payload)
  }

  final case class ReplyTopic(requestName: Expression[String], topicName: Expression[String], payload: Expression[Any]) {
    def replyTopic(replyTopic: Expression[String]): RequestReplyDslBuilder =
      RequestReplyDslBuilder(KafkaAttributes(requestName, topicName, payload = payload),
        new RequestReplyBuilder(_, replyTopic))
  }
}

final case class RequestReplyDslBuilder(attributes: KafkaAttributes, factory: KafkaAttributes => ActionBuilder) {

  def key(key: Expression[String]) = this.modify(_.attributes.key).setTo(Some(key))

  def protobufOutput[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message](companion: ProtoBufScalaPB[ScalaPB, JavaPB]) = {
    this.modify(_.attributes.protoAttributes).setTo(Some(SerdeScalaPB.create(companion)))
  }

  def header(name: Expression[String], value: Expression[String]) =
    this.modify(_.attributes.headers)(_ + (name -> value))

  def headers(newHeaders: Map[String, String]) =
    this.modify(_.attributes.headers)(_ ++ newHeaders.map { v => v._1.el[String] -> v._2.el[String] })

  def check(checks: KafkaCheck*): RequestReplyDslBuilder = {
    require(!checks.contains(null), "Checks can't contain null elements. Forward reference issue?")
    this.modify(_.attributes.checks)(_ ::: checks.toList)
  }

  def checkIf(condition: Expression[Boolean])(thenChecks: KafkaCheck*): RequestReplyDslBuilder =
    check(thenChecks.map(_.checkIf(condition)): _*)

  def checkIf(condition: (KafkaResponseMessage, Session) => Validation[Boolean])(thenChecks: KafkaCheck*): RequestReplyDslBuilder =
    check(thenChecks.map(_.checkIf(condition)): _*)

  def build: ActionBuilder = factory(attributes)
}

object OnlyConsumeDslBuilder {
  final case class Topic(requestName: Expression[String]) {
    def readTopic(readTopic: Expression[String]): TrackPayload = TrackPayload(requestName, readTopic)
  }

  final case class TrackPayload(requestName: Expression[String], readTopic: Expression[String]) {
    def payloadForTracking(payload: Expression[Any]): OnlyConsumeDslBuilder =
      OnlyConsumeDslBuilder(KafkaAttributes(requestName, readTopic, payload), new OnlyConsumeBuilder(_, readTopic)).onlyConsume
  }
}

final case class OnlyConsumeDslBuilder(attributes: KafkaAttributes, factory: KafkaAttributes => ActionBuilder) {

  def onlyConsume = this.modify(_.attributes.onlyConsume).setTo(true)

  def keyForTracking(key: Expression[String]) = this.modify(_.attributes.key).setTo(Some(key))

  def startTime(startTime: Expression[Long]) = this.modify(_.attributes.startTime).setTo(startTime)

  def protobufOutput[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message](companion: ProtoBufScalaPB[ScalaPB, JavaPB]) = {
    this.modify(_.attributes.protoAttributes).setTo(Some(SerdeScalaPB.create(companion)))
  }

  def headerForTracking(name: Expression[String], value: Expression[String]) =
    this.modify(_.attributes.headers)(_ + (name -> value))

  def headersForTracking(newHeaders: Map[String, String]) =
    this.modify(_.attributes.headers)(_ ++ newHeaders.map { v => v._1.el[String] -> v._2.el[String] })

  def check(checks: KafkaCheck*): OnlyConsumeDslBuilder = {
    require(!checks.contains(null), "Checks can't contain null elements. Forward reference issue?")
    this.modify(_.attributes.checks)(_ ::: checks.toList)
  }

  def checkIf(condition: Expression[Boolean])(thenChecks: KafkaCheck*): OnlyConsumeDslBuilder =
    check(thenChecks.map(_.checkIf(condition)): _*)

  def checkIf(condition: (KafkaResponseMessage, Session) => Validation[Boolean])(thenChecks: KafkaCheck*): OnlyConsumeDslBuilder =
    check(thenChecks.map(_.checkIf(condition)): _*)

  def build: ActionBuilder = factory(attributes)
}

