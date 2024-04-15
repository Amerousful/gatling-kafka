package io.github.amerousful.kafka.check

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.commons.validation.FailureWrapper
import io.gatling.core.check.Check.PreparedCache
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.check.xpath.XPathCheckType
import io.gatling.core.check.{Check, CheckBuilder, CheckMaterializer, CheckResult, TypedCheckIfMaker, UntypedCheckIfMaker}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.json.JsonParsers
import io.gatling.core.session.{Expression, Session}
import io.github.amerousful.kafka._
import io.github.amerousful.kafka.check.header.{KafkaHeaderCheckBuilder, KafkaHeaderCheckMaterializer, KafkaHeaderCheckType}
import net.sf.saxon.s9api.XdmNode
import org.apache.kafka.clients.consumer.ConsumerRecord
import io.github.amerousful.kafka.check.protobuf.{KafkaProtobufCheckBuilder, KafkaProtobufCheckMaterializer, KafkaProtobufCheckType}
import scalapb.GeneratedMessage

import scala.annotation.implicitNotFound

trait KafkaCheckSupport {

  def simpleCheck(f: KafkaResponseMessage => Boolean): KafkaCheck =
    Check.Simple((response: ConsumerRecord[String, _], _: Session, _: PreparedCache) => {
      if (f(response)) {
        CheckResult.NoopCheckResultSuccess
      } else {
        "Kafka check failed".failure
      }
    }, None)

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for Kafka.")
  implicit def checkBuilder2KafkaCheck[T, P]
  (checkBuilder: CheckBuilder[T, P])
  (implicit materializer: CheckMaterializer[T, KafkaCheck, KafkaResponseMessage, P]): KafkaCheck =
    checkBuilder.build(materializer)

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for Kafka.")
  implicit def validate2KafkaCheck[T, P, X]
  (validate: CheckBuilder.Validate[T, P, X])
  (implicit materializer: CheckMaterializer[T, KafkaCheck, KafkaResponseMessage, P]): KafkaCheck =
    validate.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for Kafka.")
  implicit def find2KafkaCheck[T, P, X]
  (find: CheckBuilder.Find[T, P, X])
  (implicit materializer: CheckMaterializer[T, KafkaCheck, KafkaResponseMessage, P]): KafkaCheck =
    find.find.exists

  // body string
  implicit def kafkaBodyStringCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[BodyStringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    KafkaCheckMaterializer.bodyString()

  // body length
  implicit def kafkaBodyLengthCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[BodyBytesCheckType, KafkaCheck, KafkaResponseMessage, Int] =
    KafkaCheckMaterializer.bodyLength()

  // body bytes
  implicit def kafkaBodyBytesCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[BodyBytesCheckType, KafkaCheck, KafkaResponseMessage, Array[Byte]] =
    KafkaCheckMaterializer.bodyBytes(configuration.core.charset)

  // substring
  implicit def kafkaSubstringCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[SubstringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    KafkaCheckMaterializer.substring()

  // json path
  implicit def kafkaJsonPathCheckMaterializer
  (implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration):
  CheckMaterializer[JsonPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    KafkaCheckMaterializer.jsonPath(jsonParsers)

  // jmes path
  implicit def kafkaJmesPathCheckMaterializer
  (implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration):
  CheckMaterializer[JmesPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    KafkaCheckMaterializer.jmesPath(jsonParsers)

  implicit val kafkaXPathMaterializer: CheckMaterializer[XPathCheckType, KafkaCheck, KafkaResponseMessage, XdmNode] =
    KafkaCheckMaterializer.Xpath

  implicit val kafkaRegexMaterializer: CheckMaterializer[RegexCheckType, KafkaCheck, KafkaResponseMessage, String] =
    KafkaCheckMaterializer.Regex

  implicit val kafkaUntypedCheckIfMaker: UntypedCheckIfMaker[KafkaCheck] = _.checkIf(_)

  implicit val kafkaTypedCheckIfMaker: TypedCheckIfMaker[KafkaResponseMessage, KafkaCheck] = _.checkIf(_)

  def header(headerName: Expression[CharSequence]): CheckBuilder.MultipleFind[KafkaHeaderCheckType, KafkaResponseMessage, String] = new KafkaHeaderCheckBuilder(headerName)
  implicit val kafkaHeaderCheckMaterializer: CheckMaterializer[KafkaHeaderCheckType, KafkaCheck, KafkaResponseMessage, KafkaResponseMessage] = KafkaHeaderCheckMaterializer.Instance

  def protobufResponse[ProtoClass <: GeneratedMessage, ExtractedType](extractFunction: ProtoClass => ExtractedType): CheckBuilder.Find[KafkaProtobufCheckType, KafkaResponseMessage, ExtractedType] = new KafkaProtobufCheckBuilder(extractFunction)
  implicit val kafkaProtobufCheckMaterializer: CheckMaterializer[KafkaProtobufCheckType, KafkaCheck, KafkaResponseMessage, KafkaResponseMessage] = KafkaProtobufCheckMaterializer.Instance

}
