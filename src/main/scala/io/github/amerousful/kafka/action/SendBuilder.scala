package io.github.amerousful.kafka.action

import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext
import io.github.amerousful.kafka.protocol.KafkaComponents
import io.github.amerousful.kafka.request.KafkaAttributes

final class SendBuilder(attributes: KafkaAttributes) extends KafkaActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {

    val kafkaComponents: KafkaComponents = components(ctx.protocolComponentsRegistry)

    new Send(
      attributes,
      kafkaComponents.kafkaProtocol,
      kafkaComponents.kafkaProducer,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next
    )
  }

}
