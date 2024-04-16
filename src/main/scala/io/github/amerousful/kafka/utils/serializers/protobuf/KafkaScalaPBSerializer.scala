package io.github.amerousful.kafka.utils.serializers.protobuf

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.protobuf.{AbstractKafkaProtobufSerializer, KafkaProtobufSerializer}
import org.apache.kafka.common.serialization.Serializer
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}

class KafkaScalaPBSerializer[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message]
  extends AbstractKafkaProtobufSerializer[JavaPB] with Serializer[ScalaPB] {

  var companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB] = _
  var kafkaProtobufSerializer: KafkaProtobufSerializer[JavaPB] = _

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]) = {
    this()
    this.companion = companion
    this.kafkaProtobufSerializer = new KafkaProtobufSerializer[JavaPB]()
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient) = {
    this()
    this.companion = companion
    this.kafkaProtobufSerializer = new KafkaProtobufSerializer[JavaPB](client)
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient,
           props: java.util.Map[String, _]) = {
    this()
    this.companion = companion
    this.kafkaProtobufSerializer = new KafkaProtobufSerializer[JavaPB](client, props)
  }

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
    kafkaProtobufSerializer.configure(configs, isKey)

  def serialize(topic: String, record: ScalaPB): Array[Byte] = {
    if (record == null) {
      null
    } else {
      kafkaProtobufSerializer.serialize(topic, companion.toJavaProto(record))
    }
  }
}
