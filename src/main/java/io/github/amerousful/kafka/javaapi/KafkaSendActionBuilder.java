
package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.Session;

import java.util.Map;
import java.util.function.Function;

import static io.gatling.javaapi.core.internal.Converters.toScalaMap;
import static io.gatling.javaapi.core.internal.Expressions.javaFunctionToExpression;
import static io.gatling.javaapi.core.internal.Expressions.toStringExpression;


public final class KafkaSendActionBuilder implements ActionBuilder {

  private final io.github.amerousful.kafka.request.SendDslBuilder wrapped;

  public KafkaSendActionBuilder(io.github.amerousful.kafka.request.SendDslBuilder wrapped) {
    this.wrapped = wrapped;
  }

  public static final class Send {
    private final io.github.amerousful.kafka.request.SendDslBuilder wrapped;

    public Send(io.github.amerousful.kafka.request.SendDslBuilder wrapped) {
      this.wrapped = wrapped;
    }
  }

  public static final class Topic {
    private final io.github.amerousful.kafka.request.SendDslBuilder.Topic wrapped;

    public Topic(io.github.amerousful.kafka.request.SendDslBuilder.Topic wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public KafkaSendActionBuilder.Payload topic(@NonNull String topicName) {
      return new KafkaSendActionBuilder.Payload(wrapped.topic(toStringExpression(topicName)));
    }

    @NonNull
    public KafkaSendActionBuilder.Payload topic(@NonNull Function<Session, String> topicName) {
      return new KafkaSendActionBuilder.Payload(wrapped.topic(javaFunctionToExpression(topicName)));
    }
  }

  public static final class Payload {
    private final io.github.amerousful.kafka.request.SendDslBuilder.Payload wrapped;

    public Payload(io.github.amerousful.kafka.request.SendDslBuilder.Payload wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public KafkaSendActionBuilder payload(@NonNull String payload) {
      return new KafkaSendActionBuilder(wrapped.payload(toStringExpression(payload)));
    }

    @NonNull
    public KafkaSendActionBuilder payload(@NonNull Function<Session, String> payload) {
      return new KafkaSendActionBuilder(wrapped.payload(javaFunctionToExpression(payload)));
    }

  }

  @NonNull
  public KafkaSendActionBuilder key(@NonNull Function<Session, String> key) {
    return new KafkaSendActionBuilder(wrapped.key(javaFunctionToExpression(key)));
  }

  @NonNull
  public KafkaSendActionBuilder key(@NonNull String key) {
    return new KafkaSendActionBuilder(wrapped.key(toStringExpression(key)));
  }

  @NonNull
  public KafkaSendActionBuilder header(@NonNull String name, @NonNull String value) {
    return new KafkaSendActionBuilder(wrapped.header(toStringExpression(name), toStringExpression(value)));
  }

  @NonNull
  public KafkaSendActionBuilder header(@NonNull String name, @NonNull Function<Session, String> value) {
    return new KafkaSendActionBuilder(wrapped.header(toStringExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public KafkaSendActionBuilder header(@NonNull Function<Session, String> name, @NonNull String value) {
    return new KafkaSendActionBuilder(wrapped.header(javaFunctionToExpression(name), toStringExpression(value)));
  }

  @NonNull
  public KafkaSendActionBuilder header(
          @NonNull Function<Session, String> name, @NonNull Function<Session, String> value) {
    return new KafkaSendActionBuilder(
            wrapped.header(javaFunctionToExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public KafkaSendActionBuilder headers(@NonNull Map<String, String> headers) {
    return new KafkaSendActionBuilder(wrapped.headers(toScalaMap(headers)));
  }

  @Override
  public io.gatling.core.action.builder.ActionBuilder asScala() {
    return wrapped.build();
  }
}
