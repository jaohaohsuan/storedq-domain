package com.grandsys.inu.storedq

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.grandsys.cluster.SingletonProps
import com.grandsys.inu.storedq.commands.CreateStoredQuery

import scala.concurrent.duration._

object StoredQueryRepoAggRoot {

  val name = "StoredQueryRepoAggRoot"

//  def singleton(role: Option[String] = None) = {
//    new SingletonProps(s"${name}Guardian", propsWithBackoff, role.getOrElse("unknown"))
//  }

  def propsWithBackoff = BackoffSupervisor.props(
    Backoff.onStop(
      childProps = Props(classOf[StoredQueryRepoAggRoot]),
      childName = name,
      minBackoff = 3.seconds,
      maxBackoff = 3 minute,
      randomFactor = 0.2
    ))
}

class StoredQueryRepoAggRoot() extends PersistentActor with ActorLogging {

  val persistenceId: String = StoredQueryRepoAggRoot.name

  val receiveCommand: Receive = {
    case PoisonPill => context stop self
    case CreateStoredQuery(id, title, tags) =>
      log.info(s"$id, $title, $tags")
      sender() ! services.CreateReply(id, message = title)
    case msg =>
      log.info(s"$msg")
  }

  val receiveRecover: Receive = {
    case RecoveryCompleted =>
      log.info("RecoveryCompleted")
    case SnapshotOffer(_, snapshot: Any) =>
    case _ =>
  }

}
