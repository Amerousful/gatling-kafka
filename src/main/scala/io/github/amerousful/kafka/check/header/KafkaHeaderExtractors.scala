package io.github.amerousful.kafka.check.header

import io.gatling.commons.validation.SuccessWrapper
import io.gatling.core.check._
import io.github.amerousful.kafka.KafkaResponseMessage

import java.nio.charset.StandardCharsets.UTF_8
import scala.jdk.CollectionConverters._


object KafkaHeaderExtractors {

  private def headersValuesConvertFromBytesToString(record: KafkaResponseMessage): Seq[String] = {
    record.headers().asScala.toSeq.map(header => new String(header.value(), UTF_8))
  }

  def find(headerName: CharSequence, occurrence: Int): FindCriterionExtractor[KafkaResponseMessage, CharSequence, String] =
    new FindCriterionExtractor[KafkaResponseMessage, CharSequence, String]("kafkaHeader", headerName, occurrence, headersValuesConvertFromBytesToString(_).lift(occurrence).success)

  def findAll(headerName: CharSequence): FindAllCriterionExtractor[KafkaResponseMessage, CharSequence, String] =
    new FindAllCriterionExtractor[KafkaResponseMessage, CharSequence, String]("kafkaHeader", headerName, headersValuesConvertFromBytesToString(_).liftSeqOption.success)

  def count(headerName: CharSequence): CountCriterionExtractor[KafkaResponseMessage, CharSequence] =
    new CountCriterionExtractor[KafkaResponseMessage, CharSequence]("kafkaHeader", headerName, headersValuesConvertFromBytesToString(_).liftSeqOption.map(_.size).success)
}
