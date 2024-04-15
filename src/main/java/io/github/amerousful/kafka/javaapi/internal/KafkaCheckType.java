package io.github.amerousful.kafka.javaapi.internal;

import io.gatling.javaapi.core.CheckBuilder;

public enum KafkaCheckType implements CheckBuilder.CheckType {
  Header,
  Protobuf,
  Simple
}
