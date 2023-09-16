package io.github.amerousful.kafka

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.github.amerousful.kafka.check.KafkaCheckSupport
import io.github.amerousful.kafka.protocol.KafkaProtocolBuilder
import io.github.amerousful.kafka.request.{KafkaDslBuilderBase, RequestReplyDslBuilder, SendDslBuilder}

trait KafkaDsl extends KafkaCheckSupport {

  def kafka(implicit configuration: GatlingConfiguration): KafkaProtocolBuilder = KafkaProtocolBuilder.Default

  def kafka(requestName: Expression[String]): KafkaDslBuilderBase = new KafkaDslBuilderBase(requestName)

  implicit def kafkaProtocolBuilder2KafkaProtocol(builder: KafkaProtocolBuilder) = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: SendDslBuilder) = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: RequestReplyDslBuilder) = builder.build
}
