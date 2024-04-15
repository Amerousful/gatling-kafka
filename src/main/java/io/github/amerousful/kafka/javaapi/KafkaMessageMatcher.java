
package io.github.amerousful.kafka.javaapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;


public interface KafkaMessageMatcher {

  @NonNull
  String requestMatchId(@NonNull ProducerRecord<String, ?> msg);

  @NonNull
  String responseMatchId(@NonNull ConsumerRecord<String, ?> msg);
}
