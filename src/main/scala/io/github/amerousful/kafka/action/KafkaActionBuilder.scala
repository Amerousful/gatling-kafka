package io.github.amerousful.kafka.action

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.github.amerousful.kafka.protocol.{KafkaComponents, KafkaProtocol}

abstract class KafkaActionBuilder extends ActionBuilder {

  protected def components(protocolComponentsRegistry: ProtocolComponentsRegistry): KafkaComponents = {
    protocolComponentsRegistry.components(KafkaProtocol.kafkaProtocolKey)
  }

}
