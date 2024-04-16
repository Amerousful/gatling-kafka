package io.github.amerousful.kafka

import io.gatling.commons.validation.Validation
import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.github.amerousful.kafka.check.KafkaCheckSupport
import io.github.amerousful.kafka.protocol.{KafkaProtocol, KafkaProtocolBuilder}
import io.github.amerousful.kafka.request.{KafkaDslBuilderBase, OnlyConsumeDslBuilder, RequestReplyDslBuilder, SendDslBuilder}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}

trait KafkaDsl extends KafkaCheckSupport {

  def kafka(implicit configuration: GatlingConfiguration): KafkaProtocolBuilder = KafkaProtocolBuilder.Default

  def kafka(requestName: Expression[String]): KafkaDslBuilderBase = new KafkaDslBuilderBase(requestName)

  implicit def kafkaProtocolBuilder2KafkaProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: SendDslBuilder): ActionBuilder = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: RequestReplyDslBuilder): ActionBuilder = builder.build

  implicit def kafkaDslBuilder2ActionBuilder(builder: OnlyConsumeDslBuilder): ActionBuilder = builder.build

  type ProtoBufScalaPB[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message] =
    GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]

  implicit def convertProto[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message](payload: ScalaPB): Validation[JavaPB] = {
    payload.companion.asInstanceOf[ProtoBufScalaPB[ScalaPB, JavaPB]].toJavaProto(payload)
  }

  implicit def convertProtoSession[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message](payload: ScalaPB)(session: Session): Validation[JavaPB] = {
    payload.companion.asInstanceOf[ProtoBufScalaPB[ScalaPB, JavaPB]].toJavaProto(payload).apply(session)
  }

}
