package io.github.amerousful.kafka.action

import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.github.amerousful.kafka.protocol.KafkaComponents
import io.github.amerousful.kafka.request.KafkaAttributes

final class OnlyConsumeBuilder(
                           attributes: KafkaAttributes,
                           replyTopic: Expression[String],
                         ) extends KafkaActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {

    val kafkaComponents: KafkaComponents = components(ctx.protocolComponentsRegistry)

    new OnlyConsume(
      attributes,
      replyTopic,
      kafkaComponents.kafkaProtocol,
      kafkaComponents.kafkaProducer,
      kafkaComponents.kafkaTrackerPoll,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next
    )

  }
}
