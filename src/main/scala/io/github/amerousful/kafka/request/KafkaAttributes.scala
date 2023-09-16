package io.github.amerousful.kafka.request

import io.gatling.core.session.Expression
import io.github.amerousful.kafka.KafkaCheck

object KafkaAttributes {
  def apply(
             requestName: Expression[String],
             topic: Expression[String],
             payload: Expression[String],
           ): KafkaAttributes =
    new KafkaAttributes(
      requestName,
      topic,
      None,
      payload,
      headers = Map.empty,
      Nil
    )
}

final case class KafkaAttributes(
                                  requestName: Expression[String],
                                  topic: Expression[String],
                                  key: Option[Expression[String]],
                                  payload: Expression[String],
                                  headers: Map[Expression[String], Expression[String]],
                                  checks: List[KafkaCheck]
                                )

