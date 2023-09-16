import io.github.amerousful.kafka.Predef._
import io.gatling.core.Predef._
import io.github.amerousful.kafka.protocol.{KafkaBroker, KafkaMatcher}
import io.github.amerousful.kafka.protocol.SaslMechanism.scram_sha_512
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.duration.DurationInt

object Example {

  val brokers = List(
    KafkaBroker("kafka.us-east-1.amazonaws.com", 9096),
    KafkaBroker("kafka.us-east-2.amazonaws.com", 9096),
    KafkaBroker("kafka.us-east-3.amazonaws.com", 9096),
  )

  object CustomMatcher extends KafkaMatcher {
    override def requestMatchId(msg: ProducerRecord[String, String]): String = ???

    override def responseMatchId(msg: ConsumerRecord[String, String]): String = ???
  }

  val kafkaProtocol = kafka
    .brokers(brokers: _*)
    .broker(KafkaBroker("kafka.us-east-4.amazonaws.com", 9096))
    .credentials(
      "admin",
      "password",
      sslEnabled = true,
      saslMechanism = scram_sha_512
    )
    .acks("1")
    .producerIdenticalSerializer("org.apache.kafka.common.serialization.StringSerializer")
    .consumerIdenticalDeserializer("org.apache.kafka.common.serialization.StringDeserializer")
    .addProducerProperty("retries", "3")
    .addConsumerProperty("heartbeat.interval.ms", "3000")
    .replyTimeout(10 seconds)
    .matchByKey()
    .matchByValue()
    .messageMatcher(CustomMatcher)
    .replyConsumerName("gatling-test-consumer")

  val kafkaFireAndForget = kafka("Kafka: fire and forget")
    .send
    .topic("input_topic")
    .payload(StringBody("#{payload}"))
    .key("#{key}")
    .headers(Map(
      "header_1" -> "#{h_value_1}",
      "header_2" -> "#{h_value_2}",
    ))

  val kafkaRequestWithReply = kafka("Kafka: request with reply")
    .requestReply
    .topic("input_topic")
    .payload("""{ "m": "#{payload}" }""")
    .replyTopic("output_topic")
    .key("#{id} #{key}")
    .check(jsonPath("$.m").is("#{payload}_1"))

}
