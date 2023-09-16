package io.github.amerousful.kafka.client

import akka.actor.ActorRef
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.github.amerousful.kafka._

class KafkaTracker(actor: ActorRef) {
  def track(
             matchId: String,
             sent: Long,
             replyTimeoutInMs: Long,
             checks: List[KafkaCheck],
             session: Session,
             next: Action,
             requestName: String
           ): Unit =
    actor ! MessageSent(
      matchId,
      sent,
      replyTimeoutInMs,
      checks,
      session,
      next,
      requestName
    )
}
