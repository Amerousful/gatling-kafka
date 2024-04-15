package io.github.amerousful.kafka.check

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.commons.validation._
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.check.xpath.{XPathCheckType, XmlParsers}
import io.gatling.core.check.{CheckMaterializer, Preparer}
import io.gatling.core.json.JsonParsers
import io.github.amerousful.kafka._
import net.sf.saxon.s9api.XdmNode

import java.nio.charset.Charset


class KafkaCheckMaterializer[T, P]
(override val preparer: Preparer[KafkaResponseMessage, P]) extends CheckMaterializer[T, KafkaCheck, KafkaResponseMessage, P](identity)

object KafkaCheckMaterializer {

  private def stringBodyPreparer(): Preparer[KafkaResponseMessage, String] =
    message => message.value().toString.success

  private def bodyLengthPreparer(): Preparer[KafkaResponseMessage, Int] = {
    message => message.value().toString.length.success
  }

  private def bodyBytesPreparer(charset: Charset): Preparer[KafkaResponseMessage, Array[Byte]] =
    message => message.value().toString.getBytes(charset).success

  private val JsonPreparerErrorMapper: String => String = "Could not parse response into a JSON: " + _

  private def jsonPreparer(jsonParsers: JsonParsers): Preparer[KafkaResponseMessage, JsonNode] =
    message =>
      safely(JsonPreparerErrorMapper) {
        jsonParsers.safeParse(message.value().toString)
      }

  def bodyString(): CheckMaterializer[BodyStringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    new KafkaCheckMaterializer[BodyStringCheckType, String](stringBodyPreparer())

  def substring(): CheckMaterializer[SubstringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    new KafkaCheckMaterializer(stringBodyPreparer())

  def bodyLength(): CheckMaterializer[BodyBytesCheckType, KafkaCheck, KafkaResponseMessage, Int] =
    new KafkaCheckMaterializer(bodyLengthPreparer())

  def bodyBytes(charset: Charset): CheckMaterializer[BodyBytesCheckType, KafkaCheck, KafkaResponseMessage, Array[Byte]] =
    new KafkaCheckMaterializer(bodyBytesPreparer(charset))

  def jmesPath(jsonParsers: JsonParsers): CheckMaterializer[JmesPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    new KafkaCheckMaterializer(jsonPreparer(jsonParsers))

  def jsonPath(jsonParsers: JsonParsers): CheckMaterializer[JsonPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    new KafkaCheckMaterializer(jsonPreparer(jsonParsers))

  val Xpath: CheckMaterializer[XPathCheckType, KafkaCheck, KafkaResponseMessage, XdmNode] = {
    val errorMapper: String => String = "Could not parse response into a DOM Document: " + _

    val preparer: Preparer[KafkaResponseMessage, XdmNode] =
      message =>
        safely(errorMapper) {
          XmlParsers.parse(message.value().toString).success
        }

    new KafkaCheckMaterializer(preparer)
  }

  val Regex: CheckMaterializer[RegexCheckType, KafkaCheck, KafkaResponseMessage, String] =
    new KafkaCheckMaterializer(stringBodyPreparer())
}
