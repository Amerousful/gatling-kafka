package io.github.amerousful.kafka.request

import io.gatling.core.session.{Expression, ExpressionSuccessWrapper}
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import io.github.amerousful.kafka.KafkaCheck

object KafkaAttributes {
  def apply(
             requestName: Expression[String],
             topic: Expression[String],
             payload: Expression[Any]
           ): KafkaAttributes =
    new KafkaAttributes(
      requestName,
      topic,
      None,
      payload,
      headers = Map.empty,
      Nil,
      false,
      0L.expressionSuccess,
      None
    )
}

final case class KafkaAttributes(
                                  requestName: Expression[String],
                                  topic: Expression[String],
                                  key: Option[Expression[String]],
                                  payload: Expression[Any],
                                  headers: Map[Expression[String], Expression[String]],
                                  checks: List[KafkaCheck],

                                  onlyConsume: Boolean,
                                  startTime: Expression[Long],

                                  protoAttributes: Option[ProtoAttributes]
                                )

final case class ProtoAttributes(
                                  valueDeserializer: Deserializer[_],
                                  javaPBClazz: Class[_]
                                )

