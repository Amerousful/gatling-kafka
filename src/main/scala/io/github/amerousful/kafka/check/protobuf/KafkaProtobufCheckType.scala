package io.github.amerousful.kafka.check.protobuf

import io.gatling.commons.validation.TryWrapper
import io.gatling.core.check._
import io.gatling.core.session.ExpressionSuccessWrapper
import io.github.amerousful.kafka.check.KafkaCheckMaterializer
import io.github.amerousful.kafka.{KafkaCheck, KafkaResponseMessage}

import scala.util.Try

trait KafkaProtobufCheckType

class KafkaProtobufCheckBuilder[ProtoClass, ExtractedType](extractFunction: ProtoClass => ExtractedType)
  extends CheckBuilder.Find.Default[KafkaProtobufCheckType, KafkaResponseMessage, ExtractedType](
    extractor = new FindExtractor[KafkaResponseMessage, ExtractedType]("protobufCheck", response => {
      Try(Some(extractFunction(response.value().asInstanceOf[ProtoClass]))).toValidation
    }).expressionSuccess,
    displayActualValue = true
  )

object KafkaProtobufCheckMaterializer {
  val Instance: CheckMaterializer[KafkaProtobufCheckType, KafkaCheck, KafkaResponseMessage, KafkaResponseMessage] =
    new KafkaCheckMaterializer[KafkaProtobufCheckType, KafkaResponseMessage](identityPreparer)
}
