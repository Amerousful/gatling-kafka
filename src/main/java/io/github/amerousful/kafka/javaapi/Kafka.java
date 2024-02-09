
package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.amerousful.kafka.request.KafkaDslBuilderBase;
import io.gatling.commons.validation.Validation;
import io.gatling.core.session.Session;
import scala.Function1;

public class Kafka {
  private final KafkaDslBuilderBase wrapped;

  public Kafka(Function1<Session, Validation<String>> name) {
    wrapped = new KafkaDslBuilderBase(name);
  }

  @NonNull
  public KafkaSendActionBuilder.Topic send() {
    return new KafkaSendActionBuilder.Topic(wrapped.send());
  }

  @NonNull
  public RequestReplyActionBuilder.Topic requestReply() {
    return new RequestReplyActionBuilder.Topic(wrapped.requestReply());
  }
}
