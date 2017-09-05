package com.grandsys.inu.storedq

import akka.actor.{ActorLogging, PoisonPill, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.grandsys.inu.storedq.models.StoredQueriesState

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

  import com.grandsys.inu.storedq.commands._
  import models.AddClauseExtensions._

  private var state = StoredQueriesState()

  val persistenceId: String = StoredQueryRepoAggRoot.name

  val receiveCommand: Receive = {
    case PoisonPill => context stop self
    case CreateStoredQuery(id, title, tags) =>
      log.info(s"$id, $title, $tags")
      sender() ! services.CreatedAck(id, message = title)
    case addClause: AddClause =>
      addClause.getEdge()


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
