import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.github.amerousful.kafka.javaapi.*
import io.github.amerousful.kafka.javaapi.KafkaDsl.*
import io.github.amerousful.kafka.javaapi.KafkaMessageMatcher
import io.github.amerousful.kafka.protocol.SaslMechanism
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*


class KafkaKotlinExample {

    private val customMatcher: KafkaMessageMatcher = object : KafkaMessageMatcher {
        override fun requestMatchId(msg: ProducerRecord<String, String>): String {
            return msg.key()
        }

        override fun responseMatchId(msg: ConsumerRecord<String, String>): String {
            return msg.key()
        }
    }

    val kafkaProtocol = kafka
            .broker(KafkaBroker("kafka.us-east-1.amazonaws.com", 9096))
            .brokers(
                    KafkaBroker("kafka.us-east-2.amazonaws.com", 9096),
                    KafkaBroker("kafka.us-east-3.amazonaws.com", 9096)
            )
            .acks("1")
            .producerIdenticalSerializer("org.apache.kafka.common.serialization.StringSerializer")
            .consumerIdenticalDeserializer("org.apache.kafka.common.serialization.StringDeserializer")
            .addProducerProperty("retries", "3")
            .addConsumerProperty("heartbeat.interval.ms", "3000")
            .credentials("admin", "password", true, SaslMechanism.plain())
            .replyTimeout(10)
            .matchByKey()
            .matchByValue()
            .messageMatcher(customMatcher)
            .replyConsumerName("gatling-test-consumer")

    //#simple
    fun checkRecordValue(record: ConsumerRecord<String?, String>): Boolean {
        return record.value() == "myValue"
    }

    val scn: ScenarioBuilder = scenario("scenario")
            .exec(
                    kafka("Kafka: fire and forget")
                            .send()
                            .topic("input_topic")
                            .payload("#{payload}")
                            .key("#{key}")
                            .header("k1", "v1")
                            .headers(Collections.singletonMap("key", "value"))
            )
            .exec(
                    kafka("Kafka: request with reply")
                            .requestReply()
                            .topic("input_topic")
                            .payload("message")
                            .replyTopic("output_topic")
                            .key("#{key}")
                            .check(CoreDsl.jsonPath("$.m").`is`("#{payload}_1"))
                            .checkIf("#{bool}")
                            .then(CoreDsl.jsonPath("$..foo"))
                            .checkIf { message: ConsumerRecord<String?, String?>?, session: Session? -> true }
                            .then(CoreDsl.jsonPath("$").`is`("hello"))
                            .check(KafkaDsl.header("header1").`in`("value1"))
                            .check(KafkaDsl.simpleCheck { record: ConsumerRecord<String?, String> -> this.checkRecordValue(record) })
            )
}