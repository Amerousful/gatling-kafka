package io.github.amerousful.kafka.protocol

import com.softwaremill.quicklens.ModifyPimp
import org.apache.kafka.clients.producer.ProducerConfig._
import org.apache.kafka.clients.consumer.ConsumerConfig._
import io.github.amerousful.kafka.protocol.SaslMechanism.SaslMechanism

import scala.concurrent.duration.FiniteDuration

final case class KafkaProtocolBuilder(kafkaProtocol: KafkaProtocol) {

  def broker(broker: KafkaBroker): KafkaProtocolBuilder = brokers(broker)

  def brokers(brokers: KafkaBroker*): KafkaProtocolBuilder = {
    val bootstrapConfig: String = brokers.map(b => b.host + ":" + b.port).mkString(",")

    addProducerProperty(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapConfig)
      .addConsumerProperty(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapConfig)
  }

  def acks(acks: String): KafkaProtocolBuilder = addProducerProperty(ACKS_CONFIG, acks)

  def producerKeySerializer(serializer: String): KafkaProtocolBuilder =
    addProducerProperty(KEY_SERIALIZER_CLASS_CONFIG, serializer)

  def producerValueSerializer(serializer: String): KafkaProtocolBuilder =
    addProducerProperty(VALUE_SERIALIZER_CLASS_CONFIG, serializer)

  def consumerKeyDeserializer(deserializer: String): KafkaProtocolBuilder =
    addConsumerProperty(KEY_DESERIALIZER_CLASS_CONFIG, deserializer)

  def consumerValueDeserializer(deserializer: String): KafkaProtocolBuilder =
    addConsumerProperty(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer)

  def producerIdenticalSerializer(serializer: String): KafkaProtocolBuilder =
    producerKeySerializer(serializer)
      .producerValueSerializer(serializer)

  def consumerIdenticalDeserializer(deserializer: String): KafkaProtocolBuilder =
    consumerKeyDeserializer(deserializer)
      .consumerValueDeserializer(deserializer)

  def credentials(username: String, password: String, sslEnabled: Boolean, saslMechanism: SaslMechanism): KafkaProtocolBuilder = {
    val protocol = sslEnabled match {
      case true => "SASL_SSL"
      case false => "SASL_PLAINTEXT"
    }
    val jaasConfigModule = saslMechanism match {
      case SaslMechanism.plain => "plain.PlainLoginModule"
      case SaslMechanism.scram_sha_256 => "scram.ScramLoginModule"
      case SaslMechanism.scram_sha_512 => "scram.ScramLoginModule"
    }
    saslMechanism.toString
    addConsumerAndProducerProperty("security.protocol", protocol)
      .addConsumerAndProducerProperty("sasl.mechanism", saslMechanism.toString)
      .addConsumerAndProducerProperty("sasl.jaas.config",
        s"""org.apache.kafka.common.security.$jaasConfigModule required username="$username" password="$password";""".stripMargin)
  }

  def addProducerProperty(key: String, value: String): KafkaProtocolBuilder = {
    this.modify(_.kafkaProtocol.producerProperties)(_ + (key -> value))
  }

  def addConsumerProperty(key: String, value: String): KafkaProtocolBuilder = {
    this.modify(_.kafkaProtocol.consumerProperties)(_ + (key -> value))
  }

  private def addConsumerAndProducerProperty(key: String, value: String): KafkaProtocolBuilder =
    addProducerProperty(key, value)
      .addConsumerProperty(key, value)

  def messageMatcher(matcher: KafkaMatcher): KafkaProtocolBuilder =
    this.modify(_.kafkaProtocol.messageMatcher).setTo(matcher)

  def matchByKey(): KafkaProtocolBuilder = messageMatcher(KafkaKeyMatcher)

  def matchByValue(): KafkaProtocolBuilder = messageMatcher(KafkaValueMatcher)

  def replyTimeout(timeout: FiniteDuration): KafkaProtocolBuilder = this.modify(_.kafkaProtocol.replyTimeout).setTo(Some(timeout))

  def replyConsumerName(name: String): KafkaProtocolBuilder = addConsumerProperty(GROUP_ID_CONFIG, name)

  def build: KafkaProtocol = kafkaProtocol
}

object KafkaProtocolBuilder {
  implicit def toKafkaProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  val Default: KafkaProtocolBuilder = KafkaProtocolBuilder(KafkaProtocol.apply())
}