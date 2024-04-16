package io.github.amerousful.kafka.utils.serializers.protobuf

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.protobuf.{AbstractKafkaProtobufDeserializer, KafkaProtobufDeserializer}
import org.apache.kafka.common.serialization.Deserializer
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}

class KafkaScalaPBDeserializer[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message]
  extends AbstractKafkaProtobufDeserializer[JavaPB] with Deserializer[ScalaPB] {

  var companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB] = _
  var kafkaProtobufDeserializer: KafkaProtobufDeserializer[JavaPB] = _

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]) = {
    this()
    this.companion = companion
    this.kafkaProtobufDeserializer = new KafkaProtobufDeserializer[JavaPB]()
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient) = {
    this()
    this.companion = companion
    this.kafkaProtobufDeserializer = new KafkaProtobufDeserializer[JavaPB](client)
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient, props: java.util.Map[String, _],
           classType: Class[JavaPB]) = {
    this(companion)
    this.kafkaProtobufDeserializer = new KafkaProtobufDeserializer[JavaPB](client, props, classType)
  }

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
    kafkaProtobufDeserializer.configure(configs, isKey)

  def deserialize(s: String, bytes: Array[Byte]): ScalaPB =
    companion.fromJavaProto(kafkaProtobufDeserializer.deserialize(s, bytes))

}
