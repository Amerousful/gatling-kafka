package io.github.amerousful.kafka

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.github.amerousful.kafka.check.KafkaCheckSupport
import io.github.amerousful.kafka.protocol.{KafkaProtocol, KafkaProtocolBuilder}
import io.github.amerousful.kafka.request.{KafkaDslBuilderBase, RequestReplyDslBuilder, SendDslBuilder}

trait KafkaDsl extends KafkaCheckSupport {

  def kafka(implicit configuration: GatlingConfiguration): KafkaProtocolBuilder = KafkaProtocolBuilder.Default

  def kafka(requestName: Expression[String]): KafkaDslBuilderBase = new KafkaDslBuilderBase(requestName)

  implicit def kafkaProtocolBuilder2KafkaProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: SendDslBuilder): ActionBuilder = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: RequestReplyDslBuilder): ActionBuilder = builder.build
}
