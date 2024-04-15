package io.github.amerousful

import io.gatling.core.check.Check
import org.apache.kafka.clients.consumer.ConsumerRecord

package object kafka {
  type KafkaCheck = Check[KafkaResponseMessage]
  type KafkaResponseMessage = ConsumerRecord[String, _]
}
