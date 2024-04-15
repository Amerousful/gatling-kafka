
import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.github.amerousful.kafka.javaapi.KafkaMessageMatcher;
import io.github.amerousful.kafka.javaapi.KafkaProtocolBuilder;
import io.github.amerousful.kafka.protocol.SaslMechanism;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collections;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.github.amerousful.kafka.javaapi.KafkaDsl.*;

public class KafkaJavaExample {

    private static final KafkaMessageMatcher customMatcher =
            new KafkaMessageMatcher() {
                @NonNull
                @Override
                public String requestMatchId(@NonNull ProducerRecord<String, ?> msg) {
                    return msg.key();
                }

                @NonNull
                @Override
                public String responseMatchId(@NonNull ConsumerRecord<String, ?> msg) {
                    return msg.key();
                }
            };

    KafkaProtocolBuilder kafkaProtocol = kafka
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
            .replyConsumerName("gatling-test-consumer");

    //#simple
    public boolean checkRecordValue(ConsumerRecord<String, ?> record) {
        return record.value().equals("myValue");
    }

    ScenarioBuilder scn =
            scenario("scenario")
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
                                    .check(jsonPath("$.m").is("#{payload}_1"))
                                    .checkIf("#{bool}")
                                    .then(jsonPath("$..foo"))
                                    .checkIf((message, session) -> true)
                                    .then(jsonPath("$").is("hello"))
                                    .check(header("header1").in("value1"))
                                    .check(simpleCheck(this::checkRecordValue))
                    )
                    .exec(
                            kafka("Kafka only consume")
                                    .onlyConsume()
                                    .readTopic("")
                                    .payloadForTracking("")
                                    .keyForTracking("")
                                    .startTime(session -> 100000000L)
                    );

}
