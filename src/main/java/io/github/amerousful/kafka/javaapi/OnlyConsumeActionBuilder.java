package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.core.Session;
import io.github.amerousful.kafka.internal.KafkaChecks;
import io.github.amerousful.kafka.internal.ScalaKafkaOnlyConsumeBuilderConditions;
import io.github.amerousful.kafka.request.OnlyConsumeDslBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.gatling.javaapi.core.internal.Converters.toScalaMap;
import static io.gatling.javaapi.core.internal.Expressions.*;

public final class OnlyConsumeActionBuilder implements ActionBuilder {

  private final OnlyConsumeDslBuilder wrapped;

  public OnlyConsumeActionBuilder(OnlyConsumeDslBuilder wrapped) {
    this.wrapped = wrapped;
  }

  public static final class Topic {
    private final OnlyConsumeDslBuilder.Topic wrapped;

    public Topic(OnlyConsumeDslBuilder.Topic wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public OnlyConsumeActionBuilder.TrackPayload readTopic(@NonNull String readTopic) {
      return new OnlyConsumeActionBuilder.TrackPayload(wrapped.readTopic(toStringExpression(readTopic)));
    }

    @NonNull
    public OnlyConsumeActionBuilder.TrackPayload readTopic(@NonNull Function<Session, String> readTopic) {
      return new OnlyConsumeActionBuilder.TrackPayload(wrapped.readTopic(javaFunctionToExpression(readTopic)));
    }
  }

  public static final class TrackPayload {
    private final OnlyConsumeDslBuilder.TrackPayload wrapped;

    public TrackPayload(OnlyConsumeDslBuilder.TrackPayload wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public OnlyConsumeActionBuilder payloadForTracking(@NonNull String payload) {
      return new OnlyConsumeActionBuilder(wrapped.payloadForTracking(toAnyExpression(payload)));
    }

    @NonNull
    public OnlyConsumeActionBuilder payloadForTracking(@NonNull Function<Session, Object> payload) {
      return new OnlyConsumeActionBuilder(wrapped.payloadForTracking(javaFunctionToExpression(payload)));
    }

  }

  @NonNull
  public OnlyConsumeActionBuilder keyForTracking(@NonNull Function<Session, String> key) {
    return new OnlyConsumeActionBuilder(wrapped.keyForTracking(javaFunctionToExpression(key)));
  }

  @NonNull
  public OnlyConsumeActionBuilder keyForTracking(@NonNull String key) {
    return new OnlyConsumeActionBuilder(wrapped.keyForTracking(toStringExpression(key)));
  }

  @NonNull
  public OnlyConsumeActionBuilder startTime(@NonNull Function<Session, Long> startTime) {
    return new OnlyConsumeActionBuilder(wrapped.startTime(javaLongFunctionToExpression(startTime)));
  }

  @NonNull
  public OnlyConsumeActionBuilder headerForTracking(@NonNull String name, @NonNull String value) {
    return new OnlyConsumeActionBuilder(
            wrapped.headerForTracking(toStringExpression(name), toStringExpression(value)));
  }

  @NonNull
  public OnlyConsumeActionBuilder headerForTracking(@NonNull String name, @NonNull Function<Session, String> value) {
    return new OnlyConsumeActionBuilder(
            wrapped.headerForTracking(toStringExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public OnlyConsumeActionBuilder headerForTracking(@NonNull Function<Session, String> name, @NonNull String value) {
    return new OnlyConsumeActionBuilder(
            wrapped.headerForTracking(javaFunctionToExpression(name), toStringExpression(value)));
  }

  @NonNull
  public OnlyConsumeActionBuilder headerForTracking(
          @NonNull Function<Session, String> name, @NonNull Function<Session, String> value) {
    return new OnlyConsumeActionBuilder(
            wrapped.headerForTracking(javaFunctionToExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public OnlyConsumeActionBuilder headersForTracking(@NonNull Map<String, String> headers) {
    return new OnlyConsumeActionBuilder(wrapped.headersForTracking(toScalaMap(headers)));
  }

  @NonNull
  public OnlyConsumeActionBuilder check(@NonNull CheckBuilder... checks) {
    return check(Arrays.asList(checks));
  }

  @NonNull
  public OnlyConsumeActionBuilder check(@NonNull List<CheckBuilder> checks) {
    return new OnlyConsumeActionBuilder(wrapped.check(KafkaChecks.toScalaChecks(checks)));
  }

  @NonNull
  public OnlyConsumeActionBuilder.UntypedCondition checkIf(@NonNull String condition) {
    return new OnlyConsumeActionBuilder.UntypedCondition(
            ScalaKafkaOnlyConsumeBuilderConditions.untyped(wrapped, condition));
  }

  @NonNull
  public OnlyConsumeActionBuilder.UntypedCondition checkIf(@NonNull Function<Session, Boolean> condition) {
    return new OnlyConsumeActionBuilder.UntypedCondition(
            ScalaKafkaOnlyConsumeBuilderConditions.untyped(wrapped, condition));
  }

  @NonNull
  public OnlyConsumeActionBuilder.TypedCondition checkIf(
          @NonNull BiFunction<ConsumerRecord<String, ?>, Session, Boolean> condition) {
    return new OnlyConsumeActionBuilder.TypedCondition(
            ScalaKafkaOnlyConsumeBuilderConditions.typed(wrapped, condition));
  }

  public static final class UntypedCondition {
    private final ScalaKafkaOnlyConsumeBuilderConditions.Untyped wrapped;

    public UntypedCondition(ScalaKafkaOnlyConsumeBuilderConditions.Untyped wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public OnlyConsumeActionBuilder then(@NonNull CheckBuilder... checks) {
      return then(Arrays.asList(checks));
    }

    @NonNull
    public OnlyConsumeActionBuilder then(@NonNull List<CheckBuilder> checks) {
      return wrapped.thenChecks(checks);
    }
  }

  public static final class TypedCondition {
    private final ScalaKafkaOnlyConsumeBuilderConditions.Typed wrapped;

    public TypedCondition(ScalaKafkaOnlyConsumeBuilderConditions.Typed wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public OnlyConsumeActionBuilder then(@NonNull CheckBuilder... checks) {
      return then(Arrays.asList(checks));
    }

    @NonNull
    public OnlyConsumeActionBuilder then(@NonNull List<CheckBuilder> checks) {
      return wrapped.then_(checks);
    }

  }

  @Override
  public io.gatling.core.action.builder.ActionBuilder asScala() {
    return wrapped.build();
  }
}
