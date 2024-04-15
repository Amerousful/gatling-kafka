package io.github.amerousful.kafka.client

import akka.actor.{Actor, Props, Timers}
import io.gatling.commons.stats._
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Failure
import io.gatling.core.action.Action
import io.gatling.core.check.Check
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.github.amerousful.kafka._

import scala.collection.mutable
import scala.concurrent.duration._


final case class MessageSent(
                              matchId: Any,
                              sent: Long,
                              replyTimeoutInMs: Long,
                              checks: List[KafkaCheck],
                              session: Session,
                              next: Action,
                              requestName: String
                            )

final case class MessageReceived(
                                  matchId: Any,
                                  received: Long,
                                  message: KafkaResponseMessage
                                )

case object TimeoutScan

object Tracker {
  def props(statsEngine: StatsEngine, clock: Clock): Props =
    Props(new Tracker(statsEngine, clock, 1 second))
}

class Tracker(statsEngine: StatsEngine, clock: Clock, replyTimeoutScanPeriod: FiniteDuration) extends Actor with Timers {
  private val sentMessages = mutable.HashMap.empty[Any, MessageSent]
  private val timedOutMessages = mutable.ArrayBuffer.empty[MessageSent]
  private var periodicTimeoutScanTriggered = false


  private def triggerPeriodicTimeoutScan(): Unit =
    if (!periodicTimeoutScanTriggered) {
      periodicTimeoutScanTriggered = true
      timers.startTimerAtFixedRate("timeoutTimer", TimeoutScan, replyTimeoutScanPeriod)
    }

  override def receive: Receive = {

    case messageSent: MessageSent =>
      sentMessages += messageSent.matchId -> messageSent
      if (messageSent.replyTimeoutInMs > 0) triggerPeriodicTimeoutScan()


    case MessageReceived(matchId, received, message) =>
      sentMessages.remove(matchId).foreach {
        case MessageSent(_, sent, _, checks, session, next, requestName) =>
          processMessage(session, sent, received, checks, message, next, requestName)
      }

    case TimeoutScan =>
      val now = clock.nowMillis
      sentMessages.valuesIterator.foreach { message =>
        val replyTimeoutInMs = message.replyTimeoutInMs
        if (replyTimeoutInMs > 0 && (now - message.sent) > replyTimeoutInMs) {
          timedOutMessages += message
        }
      }

      for (MessageSent(matchId, sent, replyTimeoutInMs, _, session, next, requestName) <- timedOutMessages) {
        sentMessages.remove(matchId)
        executeNext(session.markAsFailed, sent, now, KO, next, requestName, Some(s"Reply timeout after $replyTimeoutInMs ms"))
      }
      timedOutMessages.clear()
  }

  private def executeNext(
                           session: Session,
                           sent: Long,
                           received: Long,
                           status: Status,
                           next: Action,
                           requestName: String,
                           message: Option[String]
                         ): Unit = {
    statsEngine.logResponse(session.scenario, session.groups, requestName, sent, received, status, None, message)
    next ! session.logGroupRequestTimings(sent, received)
  }

  private def processMessage(
                              session: Session,
                              sent: Long,
                              received: Long,
                              checks: List[KafkaCheck],
                              message: KafkaResponseMessage,
                              next: Action,
                              requestName: String
                            ): Unit = {


    val (newSession, error) = Check.check(message, session, checks)
    error match {
      case Some(Failure(errorMessage)) => executeNext(newSession.markAsFailed, sent, received, KO, next, requestName, Some(errorMessage))
      case _ => executeNext(newSession, sent, received, OK, next, requestName, None)
    }
  }

}
