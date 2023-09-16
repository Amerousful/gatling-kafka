package io.github.amerousful.kafka.check

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.commons.validation.FailureWrapper
import io.gatling.core.check.Check.PreparedCache
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.check.xpath.XPathCheckType
import io.gatling.core.check.{Check, CheckBuilder, CheckMaterializer, CheckResult, TypedCheckIfMaker, UntypedCheckIfMaker}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.json.JsonParsers
import io.gatling.core.session.Session
import net.sf.saxon.s9api.XdmNode
import org.apache.kafka.clients.consumer.ConsumerRecord
import io.github.amerousful.kafka._

import scala.annotation.implicitNotFound

trait KafkaCheckSupport {

  def simpleCheck(f: KafkaResponseMessage => Boolean): KafkaCheck =
    Check.Simple((response: ConsumerRecord[String, String], _: Session, _: PreparedCache) => {
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

  implicit def kafkaBodyStringCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[BodyStringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    KafkaCheckMaterializer.bodyString()

  implicit def kafkaBodyLengthCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[BodyBytesCheckType, KafkaCheck, KafkaResponseMessage, Int] =
    KafkaCheckMaterializer.bodyLength()

  implicit def kafkaSubstringCheckMaterializer
  (implicit configuration: GatlingConfiguration): CheckMaterializer[SubstringCheckType, KafkaCheck, KafkaResponseMessage, String] =
    KafkaCheckMaterializer.substring()

  implicit def kafkaJsonPathCheckMaterializer
  (implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration):
  CheckMaterializer[JsonPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    KafkaCheckMaterializer.jsonPath(jsonParsers)

  implicit def kafkaJmesPathCheckMaterializer
  (implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration):
  CheckMaterializer[JmesPathCheckType, KafkaCheck, KafkaResponseMessage, JsonNode] =
    KafkaCheckMaterializer.jmesPath(jsonParsers)

  implicit val kafkaXPathMaterializer: CheckMaterializer[XPathCheckType, KafkaCheck, KafkaResponseMessage, XdmNode] =
    KafkaCheckMaterializer.Xpath

  implicit val kafkaUntypedCheckIfMaker: UntypedCheckIfMaker[KafkaCheck] = _.checkIf(_)

  implicit val kafkaTypedCheckIfMaker: TypedCheckIfMaker[KafkaResponseMessage, KafkaCheck] = _.checkIf(_)
}
