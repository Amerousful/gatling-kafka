package io.github.amerousful.kafka.client

import akka.kafka.RestrictedConsumer
import akka.kafka.scaladsl.PartitionAssignmentHandler
import io.github.amerousful.kafka.action.KafkaLogging
import org.apache.kafka.common.TopicPartition

import scala.jdk.CollectionConverters._

case class WaitRebalancing(consumerName: String, callback: () => Unit) extends PartitionAssignmentHandler with KafkaLogging {
  override def onRevoke(revokedTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit = {}

  override def onAssign(assignedTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit = {
    logger.debug(s"Partitions [${assignedTps.mkString(", ")}] have been assigned for consumer [$consumerName]")
    callback()
  }

  override def onLost(lostTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit = {}

  override def onStop(currentTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit = {}

}
