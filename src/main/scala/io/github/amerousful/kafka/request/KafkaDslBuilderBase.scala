package io.github.amerousful.kafka.request

import com.softwaremill.quicklens.ModifyPimp
import io.gatling.commons.validation.Validation
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.el.El
import io.gatling.core.session.{Expression, Session}
import io.github.amerousful.kafka.action.{RequestReplyBuilder, SendBuilder}
import io.github.amerousful.kafka.{KafkaCheck, KafkaResponseMessage}

final class KafkaDslBuilderBase(requestName: Expression[String]) {
  def send: SendDslBuilder.Topic = SendDslBuilder.Topic(requestName)

  def requestReply: RequestReplyDslBuilder.Topic = RequestReplyDslBuilder.Topic(requestName)
}

object SendDslBuilder {
  final case class Topic(requestName: Expression[String]) {
    def topic(topicName: Expression[String]): Payload = Payload(requestName, topicName)
  }

  final case class Payload(requestName: Expression[String], topicName: Expression[String]) {
    def payload(payload: Expression[String]) = SendDslBuilder(
      KafkaAttributes(requestName, topicName, payload = payload),
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
    def payload(payload: Expression[String]): ReplyTopic = ReplyTopic(requestName, topicName, payload)
  }

  final case class ReplyTopic(requestName: Expression[String], topicName: Expression[String], payload: Expression[String]) {
    def replyTopic(replyTopic: Expression[String]): RequestReplyDslBuilder =
      RequestReplyDslBuilder(KafkaAttributes(requestName, topicName, payload = payload),
        new RequestReplyBuilder(_, replyTopic))
  }
}

final case class RequestReplyDslBuilder(attributes: KafkaAttributes, factory: KafkaAttributes => ActionBuilder) {

  def key(key: Expression[String]) = this.modify(_.attributes.key).setTo(Some(key))

  def payload(payload: Expression[String]) = this.modify(_.attributes.payload).setTo(payload)

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

