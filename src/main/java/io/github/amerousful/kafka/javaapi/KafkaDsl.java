package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.core.check.Check;
import io.gatling.core.check.CheckMaterializer;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.core.Session;
import io.github.amerousful.kafka.javaapi.internal.KafkaCheckType;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.Function;

import static io.gatling.javaapi.core.internal.Converters.toScalaFunction;
import static io.gatling.javaapi.core.internal.Expressions.*;
import static io.gatling.javaapi.core.internal.Expressions.toExpression;

public final class KafkaDsl {
    private KafkaDsl() {
    }

    public static KafkaProtocolBuilder kafka =
            new KafkaProtocolBuilder(io.github.amerousful.kafka.Predef.kafka(io.gatling.core.Predef.configuration()));

    @NonNull
    public static KafkaBroker KafkaBroker(@NonNull String host, int port) {
        return new KafkaBroker(new io.github.amerousful.kafka.protocol.KafkaBroker(host, port));
    }

    @NonNull
    public static Kafka kafka(@NonNull String name) {
        return new Kafka(toStringExpression(name));
    }

    @NonNull
    public static Kafka kafka(@NonNull Function<Session, String> name) {
        return new Kafka(javaFunctionToExpression(name));
    }

    @NonNull
    public static CheckBuilder.MultipleFind<String> header(@NonNull CharSequence name) {
        return new CheckBuilder.MultipleFind.Default<>(
                io.github.amerousful.kafka.Predef.header(toStaticValueExpression(name)),
                KafkaCheckType.Header,
                String.class,
                null);
    }

    @NonNull
    public static CheckBuilder.MultipleFind<String> header(@NonNull String name) {
        return new CheckBuilder.MultipleFind.Default<>(
                io.github.amerousful.kafka.Predef.header(toExpression(name, CharSequence.class)),
                KafkaCheckType.Header,
                String.class,
                null);
    }

    @NonNull
    public static CheckBuilder.MultipleFind<String> header(
            @NonNull Function<Session, CharSequence> name) {
        return new CheckBuilder.MultipleFind.Default<>(
                io.github.amerousful.kafka.Predef.header(javaFunctionToExpression(name)),
                KafkaCheckType.Header,
                String.class,
                null);
    }

    @SuppressWarnings("rawtypes")
    public static CheckBuilder simpleCheck(Function<ConsumerRecord<String, String>, Boolean> f) {
        return new CheckBuilder() {
            @Override
            public io.gatling.core.check.CheckBuilder<?, ?> asScala() {
                return new io.gatling.core.check.CheckBuilder() {
                    @Override
                    public Check<?> build(CheckMaterializer materializer) {
                        return io.github.amerousful.kafka.Predef.simpleCheck(toScalaFunction(f::apply));
                    }
                };
            }

            @Override
            public CheckType type() {
                return KafkaCheckType.Simple;
            }
        };
    }
}
