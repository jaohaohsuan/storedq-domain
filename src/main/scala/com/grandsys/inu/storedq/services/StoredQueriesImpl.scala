package com.grandsys.inu.storedq.services

import akka.actor.ActorRef
import com.grandsys.inu.storedq.commands.{AddClause, CreateStoredQuery}
import akka.pattern.ask
import akka.util.Timeout
import com.grandsys.inu.storedq.services.StoredQueriesGrpc.StoredQueries

import scala.concurrent.Future
import scala.concurrent.duration._

class StoredQueriesImpl(proxyActor: ActorRef) extends StoredQueries {

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def create(request: CreateStoredQuery): Future[CreatedAck] = {
    ask(proxyActor, request).mapTo[CreatedAck]
  }

  override def addClauses(request: AddClause): Future[CreatedAck] = {
    Future.successful(CreatedAck(id = "-1", message = "oops"))
  }

}
