import io.github.amerousful.kafka.Predef._
import io.gatling.core.Predef._
import io.github.amerousful.kafka.protocol.{KafkaBroker, KafkaMatcher}
import io.github.amerousful.kafka.protocol.SaslMechanism.scram_sha_512
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.duration.DurationInt

object KafkaScalaExample {

  val brokers = List(
    KafkaBroker("kafka.us-east-1.amazonaws.com", 9096),
    KafkaBroker("kafka.us-east-2.amazonaws.com", 9096),
    KafkaBroker("kafka.us-east-3.amazonaws.com", 9096),
  )

  object CustomMatcher extends KafkaMatcher {
    override def requestMatchId(msg: ProducerRecord[String, _]): String = ???

    override def responseMatchId(msg: ConsumerRecord[String, _]): String = ???
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
    .ssl(
      protocol = "SASL_SSL",
      keystoreLocation = "/var/private/ssl/kafka.keystore.jks",
      keystorePassword = "connector1234",
      keyPassword = "connector1234"
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
    .header("headerKey1", "headerValue1")
    .check(
      jsonPath("$.m").is("#{payload}_1"),
      header("headerKey1").is("headerValue1")
    )
    .check(regex(""))
    .check(simpleCheck {
      // Custom check allows you to verify any entity of the record according to your needs.
      record => true
    })

  val kafkaProtobuf = kafka("Kafka: protobuf")
    .requestReply
    .topic("#{input_topic}")
    .payload(
      AuthLocal("token", 12345, "abc@xyz.com")
    )
    .replyTopic("#{output_topic}")
    .key("WS_PDF.#{user_id}")
    .protobufOutput(AuthLocal)
    .check(
      protobufResponse((auth: AuthLocal) => auth.id) is 12345
    )

  val onlyConsume = kafka("Kafka: Only consume")
    .onlyConsume
    .readTopic("#{output_topic}")
    .payloadForTracking {
      "payload"
    }
    .keyForTracking("key")
    .startTime("#{currentTimeMillis()}")

}
