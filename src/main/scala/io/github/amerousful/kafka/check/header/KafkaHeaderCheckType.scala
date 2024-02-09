package io.github.amerousful.kafka.check.header

import io.gatling.core.check._
import io.gatling.core.session.{Expression, RichExpression}
import io.github.amerousful.kafka.check.KafkaCheckMaterializer
import io.github.amerousful.kafka.{KafkaCheck, KafkaResponseMessage}

trait KafkaHeaderCheckType

class KafkaHeaderCheckBuilder(headerName: Expression[CharSequence])
  extends CheckBuilder.MultipleFind.Default[KafkaHeaderCheckType, KafkaResponseMessage, String](displayActualValue = true) {
  override protected def findExtractor(occurrence: Int): Expression[Extractor[KafkaResponseMessage, String]] = headerName.map(KafkaHeaderExtractors.find(_, occurrence))
  override protected def findAllExtractor: Expression[Extractor[KafkaResponseMessage, Seq[String]]] = headerName.map(KafkaHeaderExtractors.findAll)
  override protected def countExtractor: Expression[Extractor[KafkaResponseMessage, Int]] = headerName.map(KafkaHeaderExtractors.count)
}

object KafkaHeaderCheckMaterializer {
  val Instance: CheckMaterializer[KafkaHeaderCheckType, KafkaCheck, KafkaResponseMessage, KafkaResponseMessage] =
    new KafkaCheckMaterializer[KafkaHeaderCheckType, KafkaResponseMessage](identityPreparer)
}
