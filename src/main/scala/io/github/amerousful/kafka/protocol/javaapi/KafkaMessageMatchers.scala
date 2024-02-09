
package io.github.amerousful.kafka.protocol.javaapi

import io.github.amerousful.kafka.javaapi.KafkaMessageMatcher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaMessageMatchers {
  def toScala(javaMatcher: KafkaMessageMatcher): io.github.amerousful.kafka.protocol.KafkaMatcher =
    new io.github.amerousful.kafka.protocol.KafkaMatcher {
      override def requestMatchId(msg: ProducerRecord[String, String]): String = javaMatcher.requestMatchId(msg)
      override def responseMatchId(msg: ConsumerRecord[String, String]): String = javaMatcher.responseMatchId(msg)
    }
}
