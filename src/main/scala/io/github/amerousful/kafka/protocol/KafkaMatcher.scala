package io.github.amerousful.kafka.protocol

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

trait KafkaMatcher {
  def requestMatchId(msg: ProducerRecord[String, _]): Any
  def responseMatchId(msg: ConsumerRecord[String, _]): Any
}

object KafkaKeyMatcher extends KafkaMatcher {
  override def requestMatchId(msg: ProducerRecord[String, _]) = msg.key()
  override def responseMatchId(msg: ConsumerRecord[String, _]) = msg.key()
}

object KafkaValueMatcher extends KafkaMatcher {
  override def requestMatchId(msg: ProducerRecord[String, _]) = msg.value()
  override def responseMatchId(msg: ConsumerRecord[String, _]) = msg.value()
}
