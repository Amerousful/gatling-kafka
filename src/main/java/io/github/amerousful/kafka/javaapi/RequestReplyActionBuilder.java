package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.javaapi.core.CheckBuilder;
import io.github.amerousful.kafka.internal.ScalaKafkaRequestReplyActionBuilderConditions;
import io.github.amerousful.kafka.request.RequestReplyDslBuilder;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.Session;
import io.github.amerousful.kafka.internal.KafkaChecks;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.gatling.javaapi.core.internal.Converters.toScalaMap;
import static io.gatling.javaapi.core.internal.Expressions.*;


public final class RequestReplyActionBuilder implements ActionBuilder {

  private final RequestReplyDslBuilder wrapped;

  public RequestReplyActionBuilder(RequestReplyDslBuilder wrapped) {
    this.wrapped = wrapped;
  }

  public static final class Topic {
    private final RequestReplyDslBuilder.Topic wrapped;

    public Topic(RequestReplyDslBuilder.Topic wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public RequestReplyActionBuilder.Payload topic(@NonNull String topicName) {
      return new RequestReplyActionBuilder.Payload(wrapped.topic(toStringExpression(topicName)));
    }

    @NonNull
    public RequestReplyActionBuilder.Payload topic(@NonNull Function<Session, String> topicName) {
      return new RequestReplyActionBuilder.Payload(wrapped.topic(javaFunctionToExpression(topicName)));
    }
  }

  public static final class Payload {
    private final RequestReplyDslBuilder.Payload wrapped;

    public Payload(RequestReplyDslBuilder.Payload wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public RequestReplyActionBuilder.ReplyTopic payload(@NonNull String payload) {
      return new RequestReplyActionBuilder.ReplyTopic(wrapped.payload(toAnyExpression(payload)));
    }

    @NonNull
    public RequestReplyActionBuilder.ReplyTopic payload(@NonNull Function<Session, Object> payload) {
      return new RequestReplyActionBuilder.ReplyTopic(wrapped.payload(javaFunctionToExpression(payload)));
    }

  }

  public static final class ReplyTopic {
    private final RequestReplyDslBuilder.ReplyTopic wrapped;

    public ReplyTopic(RequestReplyDslBuilder.ReplyTopic wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public RequestReplyActionBuilder replyTopic(@NonNull String replyTopic) {
      return new RequestReplyActionBuilder(wrapped.replyTopic(toStringExpression(replyTopic)));
    }

    @NonNull
    public RequestReplyActionBuilder replyTopic(@NonNull Function<Session, String> replyTopic) {
      return new RequestReplyActionBuilder(wrapped.replyTopic(javaFunctionToExpression(replyTopic)));
    }

  }

  @NonNull
  public RequestReplyActionBuilder key(@NonNull Function<Session, String> key) {
    return new RequestReplyActionBuilder(wrapped.key(javaFunctionToExpression(key)));
  }

  @NonNull
  public RequestReplyActionBuilder key(@NonNull String key) {
    return new RequestReplyActionBuilder(wrapped.key(toStringExpression(key)));
  }

  @NonNull
  public RequestReplyActionBuilder header(@NonNull String name, @NonNull String value) {
    return new RequestReplyActionBuilder(wrapped.header(toStringExpression(name), toStringExpression(value)));
  }

  @NonNull
  public RequestReplyActionBuilder header(@NonNull String name, @NonNull Function<Session, String> value) {
    return new RequestReplyActionBuilder(wrapped.header(toStringExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public RequestReplyActionBuilder header(@NonNull Function<Session, String> name, @NonNull String value) {
    return new RequestReplyActionBuilder(wrapped.header(javaFunctionToExpression(name), toStringExpression(value)));
  }

  @NonNull
  public RequestReplyActionBuilder header(
          @NonNull Function<Session, String> name, @NonNull Function<Session, String> value) {
    return new RequestReplyActionBuilder(
            wrapped.header(javaFunctionToExpression(name), javaFunctionToExpression(value)));
  }

  @NonNull
  public RequestReplyActionBuilder headers(@NonNull Map<String, String> headers) {
    return new RequestReplyActionBuilder(wrapped.headers(toScalaMap(headers)));
  }

  @NonNull
  public RequestReplyActionBuilder check(@NonNull CheckBuilder... checks) {
    return check(Arrays.asList(checks));
  }

  @NonNull
  public RequestReplyActionBuilder check(@NonNull List<CheckBuilder> checks) {
    return new RequestReplyActionBuilder(wrapped.check(KafkaChecks.toScalaChecks(checks)));
  }

  @NonNull
  public RequestReplyActionBuilder.UntypedCondition checkIf(@NonNull String condition) {
    return new RequestReplyActionBuilder.UntypedCondition(
            ScalaKafkaRequestReplyActionBuilderConditions.untyped(wrapped, condition));
  }

  @NonNull
  public RequestReplyActionBuilder.UntypedCondition checkIf(@NonNull Function<Session, Boolean> condition) {
    return new RequestReplyActionBuilder.UntypedCondition(
            ScalaKafkaRequestReplyActionBuilderConditions.untyped(wrapped, condition));
  }

  @NonNull
  public RequestReplyActionBuilder.TypedCondition checkIf(
          @NonNull BiFunction<ConsumerRecord<String, ?>, Session, Boolean> condition) {
    return new RequestReplyActionBuilder.TypedCondition(
            ScalaKafkaRequestReplyActionBuilderConditions.typed(wrapped, condition));
  }

  public static final class UntypedCondition {
    private final ScalaKafkaRequestReplyActionBuilderConditions.Untyped wrapped;

    public UntypedCondition(ScalaKafkaRequestReplyActionBuilderConditions.Untyped wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public RequestReplyActionBuilder then(@NonNull CheckBuilder... checks) {
      return then(Arrays.asList(checks));
    }

    @NonNull
    public RequestReplyActionBuilder then(@NonNull List<CheckBuilder> checks) {
      return wrapped.thenChecks(checks);
    }
  }

  public static final class TypedCondition {
    private final ScalaKafkaRequestReplyActionBuilderConditions.Typed wrapped;

    public TypedCondition(ScalaKafkaRequestReplyActionBuilderConditions.Typed wrapped) {
      this.wrapped = wrapped;
    }

    @NonNull
    public RequestReplyActionBuilder then(@NonNull CheckBuilder... checks) {
      return then(Arrays.asList(checks));
    }

    @NonNull
    public RequestReplyActionBuilder then(@NonNull List<CheckBuilder> checks) {
      return wrapped.then_(checks);
    }

  }

  @Override
  public io.gatling.core.action.builder.ActionBuilder asScala() {
    return wrapped.build();
  }
}
