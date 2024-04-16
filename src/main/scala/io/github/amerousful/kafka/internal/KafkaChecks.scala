package io.github.amerousful.kafka.internal

import java.{util => ju}
import scala.jdk.CollectionConverters._
import io.gatling.core.{Predef => CorePredef}
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.javaapi.core.internal.CoreCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.check.xpath.XPathCheckType
import io.github.amerousful.kafka.{KafkaCheck, KafkaResponseMessage, Predef => KafkaPredef}
import com.fasterxml.jackson.databind.JsonNode
import io.gatling.core.check.regex.RegexCheckType
import io.github.amerousful.kafka.check.header.KafkaHeaderCheckType
import io.github.amerousful.kafka.check.protobuf.KafkaProtobufCheckType
import io.github.amerousful.kafka.javaapi.internal.KafkaCheckType
import net.sf.saxon.s9api.XdmNode

object KafkaChecks {

  private def toScalaCheck(javaCheck: io.gatling.javaapi.core.CheckBuilder) = {
    val scalaCheck = javaCheck.asScala
    javaCheck.`type` match {
      case CoreCheckType.BodyString => scalaCheck.asInstanceOf[CheckBuilder[BodyStringCheckType, String]].build(KafkaPredef.kafkaBodyStringCheckMaterializer(CorePredef.configuration))
      case CoreCheckType.BodyLength => scalaCheck.asInstanceOf[CheckBuilder[BodyBytesCheckType, Int]].build(KafkaPredef.kafkaBodyLengthCheckMaterializer(CorePredef.configuration))
      case CoreCheckType.BodyBytes => scalaCheck.asInstanceOf[CheckBuilder[BodyBytesCheckType, Array[Byte]]].build(KafkaPredef.kafkaBodyBytesCheckMaterializer(CorePredef.configuration))
      case CoreCheckType.Substring => scalaCheck.asInstanceOf[CheckBuilder[SubstringCheckType, String]].build(KafkaPredef.kafkaSubstringCheckMaterializer(CorePredef.configuration))
      case CoreCheckType.JsonPath => scalaCheck.asInstanceOf[CheckBuilder[JsonPathCheckType, JsonNode]].build(KafkaPredef.kafkaJsonPathCheckMaterializer(CorePredef.defaultJsonParsers, CorePredef.configuration))
      case CoreCheckType.JmesPath => scalaCheck.asInstanceOf[CheckBuilder[JmesPathCheckType, JsonNode]].build(KafkaPredef.kafkaJmesPathCheckMaterializer(CorePredef.defaultJsonParsers, CorePredef.configuration))
      case CoreCheckType.XPath => scalaCheck.asInstanceOf[CheckBuilder[XPathCheckType, XdmNode]].build(KafkaPredef.kafkaXPathMaterializer)
      case CoreCheckType.Regex => scalaCheck.asInstanceOf[CheckBuilder[RegexCheckType, String]].build(KafkaPredef.kafkaRegexMaterializer)
      case KafkaCheckType.Header => scalaCheck.asInstanceOf[CheckBuilder[KafkaHeaderCheckType, KafkaResponseMessage]].build(KafkaPredef.kafkaHeaderCheckMaterializer)
//      case KafkaCheckType.Protobuf => scalaCheck.asInstanceOf[CheckBuilder[KafkaProtobufCheckType, KafkaResponseMessage]].build(KafkaPredef.kafkaProtobufCheckMaterializer)
      case KafkaCheckType.Simple => scalaCheck.build(null).asInstanceOf[KafkaCheck]
      case unknown => throw new IllegalArgumentException(s"Kafka DSL doesn't support $unknown")
    }
  }

  def toScalaChecks(javaChecks: ju.List[io.gatling.javaapi.core.CheckBuilder]): Seq[KafkaCheck] =
    javaChecks.asScala.map(toScalaCheck).toSeq
}
