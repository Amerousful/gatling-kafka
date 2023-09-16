package io.github.amerousful.kafka.action

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.ProducerRecord

trait KafkaLogging extends StrictLogging {
  def logMessage(text: => String, msg: ProducerRecord[String, String]): Unit = {
    logger.debug(text)
    logger.trace(msg.toString)
  }
}
