package io.github.amerousful.kafka.protocol

object SaslMechanism extends Enumeration {
  type SaslMechanism = Value

  val plain = Value("PLAIN")
  val scram_sha_256 = Value("SCRAM-SHA-256")
  val scram_sha_512 = Value("SCRAM-SHA-512")

}
