package io.github.amerousful.kafka.protocol

object AutoOffsetReset extends Enumeration {
  type AutoOffsetReset = Value

  val latest = Value("latest")
  val earliest = Value("earliest")
  val none = Value("none")

}
