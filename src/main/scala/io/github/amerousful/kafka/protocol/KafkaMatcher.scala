package io.github.amerousful.kafka.protocol

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

trait KafkaMatcher {
  def requestMatchId(msg: ProducerRecord[String, String]): String
  def responseMatchId(msg: ConsumerRecord[String, String]): String
}

object KafkaKeyMatcher extends KafkaMatcher {
  override def requestMatchId(msg: ProducerRecord[String, String]): String = msg.key()
  override def responseMatchId(msg: ConsumerRecord[String, String]): String = msg.key()
}

object KafkaValueMatcher extends KafkaMatcher {
  override def requestMatchId(msg: ProducerRecord[String, String]): String = msg.value()
  override def responseMatchId(msg: ConsumerRecord[String, String]): String = msg.value()
}
