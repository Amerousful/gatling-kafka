package io.github.amerousful.kafka.utils.serdes.protobuf

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.github.amerousful.kafka.utils.serializers.protobuf.{KafkaScalaPBDeserializer, KafkaScalaPBSerializer}
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}

class KafkaScalaPBSerde[ScalaPB <: GeneratedMessage, JavaPB <: com.google.protobuf.Message] extends Serde[ScalaPB] {

  var inner: Serde[ScalaPB] = _

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]) = {
    this()
    inner = Serdes.serdeFrom(
      new KafkaScalaPBSerializer[ScalaPB, JavaPB](companion),
      new KafkaScalaPBDeserializer[ScalaPB, JavaPB](companion)
    )
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient) = {
    this()
    inner = Serdes.serdeFrom(
      new KafkaScalaPBSerializer[ScalaPB, JavaPB](companion, client),
      new KafkaScalaPBDeserializer[ScalaPB, JavaPB](companion, client)
    )
  }

  def this(companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB],
           client: SchemaRegistryClient,
           props: java.util.Map[String, _],
           classType: Class[JavaPB]) = {
    this()
    inner = Serdes.serdeFrom(
      new KafkaScalaPBSerializer[ScalaPB, JavaPB](companion, client, props),
      new KafkaScalaPBDeserializer[ScalaPB, JavaPB](companion, client, props, classType)
    )
  }

  override def configure(serdeConfigs: java.util.Map[String, _], isSerdeForRecordKeys: Boolean): Unit = {
    inner.serializer().configure(serdeConfigs, isSerdeForRecordKeys)
    inner.deserializer().configure(serdeConfigs, isSerdeForRecordKeys)
  }

  override def serializer(): Serializer[ScalaPB] = inner.serializer()

  override def deserializer(): Deserializer[ScalaPB] = inner.deserializer()

  override def close(): Unit = {
    inner.serializer().close()
    inner.deserializer().close()
  }

}
