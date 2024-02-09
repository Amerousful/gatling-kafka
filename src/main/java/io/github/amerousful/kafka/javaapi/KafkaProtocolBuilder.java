package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.core.protocol.Protocol;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.github.amerousful.kafka.protocol.javaapi.KafkaMessageMatchers;
import scala.Enumeration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.internal.Converters.toScalaDuration;
import static io.gatling.javaapi.core.internal.Converters.toScalaSeq;

public final class KafkaProtocolBuilder implements ProtocolBuilder {

    private final io.github.amerousful.kafka.protocol.KafkaProtocolBuilder wrapped;

    KafkaProtocolBuilder(io.github.amerousful.kafka.protocol.KafkaProtocolBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @NonNull
    public KafkaProtocolBuilder broker(KafkaBroker broker) {
        return new KafkaProtocolBuilder(wrapped.broker(broker.asScala()));
    }

    @NonNull
    public KafkaProtocolBuilder brokers(KafkaBroker... brokers) {
        List<io.github.amerousful.kafka.protocol.KafkaBroker> listOfBrokersAsScala =
                Arrays.stream(brokers).map(KafkaBroker::asScala).collect(Collectors.toList());
        return new KafkaProtocolBuilder(wrapped.brokers(toScalaSeq(listOfBrokersAsScala)));
    }

    @NonNull
    public KafkaProtocolBuilder acks(@NonNull String acks) {
        return new KafkaProtocolBuilder(wrapped.acks(acks));
    }

    @NonNull
    public KafkaProtocolBuilder producerKeySerializer(@NonNull String serializer) {
        return new KafkaProtocolBuilder(wrapped.producerKeySerializer(serializer));
    }

    @NonNull
    public KafkaProtocolBuilder producerValueSerializer(@NonNull String serializer) {
        return new KafkaProtocolBuilder(wrapped.producerValueSerializer(serializer));
    }

    @NonNull
    public KafkaProtocolBuilder consumerKeyDeserializer(@NonNull String deserializer) {
        return new KafkaProtocolBuilder(wrapped.consumerKeyDeserializer(deserializer));
    }

    @NonNull
    public KafkaProtocolBuilder consumerValueDeserializer(@NonNull String deserializer) {
        return new KafkaProtocolBuilder(wrapped.consumerValueDeserializer(deserializer));
    }

    @NonNull
    public KafkaProtocolBuilder consumerIdenticalDeserializer(@NonNull String deserializer) {
        return consumerKeyDeserializer(deserializer)
                .consumerValueDeserializer(deserializer);
    }

    @NonNull
    public KafkaProtocolBuilder producerIdenticalSerializer(@NonNull String serializer) {
        return new KafkaProtocolBuilder(wrapped.producerIdenticalSerializer(serializer));
    }

    @NonNull
    public KafkaProtocolBuilder addProducerProperty(@NonNull String key, @NonNull String value) {
        return new KafkaProtocolBuilder(wrapped.addProducerProperty(key, value));
    }

    @NonNull
    public KafkaProtocolBuilder addConsumerProperty(@NonNull String key, @NonNull String value) {
        return new KafkaProtocolBuilder(wrapped.addConsumerProperty(key, value));
    }

    @NonNull
    public KafkaProtocolBuilder credentials(@NonNull String username, @NonNull String password, boolean sslEnabled, @NonNull Enumeration.Value saslMechanism) {
        return new KafkaProtocolBuilder(wrapped.credentials(username, password, sslEnabled, saslMechanism));
    }

    @NonNull
    public KafkaProtocolBuilder replyTimeout(long timeout) {
        return replyTimeout(Duration.ofSeconds(timeout));
    }

    @NonNull
    public KafkaProtocolBuilder replyTimeout(@NonNull Duration timeout) {
        return new KafkaProtocolBuilder(wrapped.replyTimeout(toScalaDuration(timeout)));
    }

    @NonNull
    public KafkaProtocolBuilder messageMatcher(@NonNull KafkaMessageMatcher matcher) {
        return new KafkaProtocolBuilder(wrapped.messageMatcher(KafkaMessageMatchers.toScala(matcher)));
    }

    @NonNull
    public KafkaProtocolBuilder matchByKey() {
        return new KafkaProtocolBuilder(wrapped.matchByKey());
    }

    @NonNull
    public KafkaProtocolBuilder matchByValue() {
        return new KafkaProtocolBuilder(wrapped.matchByValue());
    }

    @NonNull
    public KafkaProtocolBuilder replyConsumerName(@NonNull String name) {
        return new KafkaProtocolBuilder(wrapped.replyConsumerName(name));
    }

    @Override
    public Protocol protocol() {
        return wrapped.build();
    }
}
