package io.github.amerousful.kafka.protocol

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session
import org.apache.kafka.clients.producer.KafkaProducer
import io.github.amerousful.kafka.client.KafkaTrackerPoll

final case class KafkaComponents(val kafkaProtocol: KafkaProtocol,
                                 val kafkaProducer: KafkaProducer[String, String],
                                 val kafkaTrackerPoll: KafkaTrackerPoll,
                                ) extends ProtocolComponents {
  override def onStart: Session => Session = Session.Identity
  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit
}
