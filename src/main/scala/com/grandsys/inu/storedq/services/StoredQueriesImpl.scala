package com.grandsys.inu.storedq.services

import akka.actor.ActorRef
import com.grandsys.inu.storedq.commands.CreateStoredQuery
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._

class StoredQueriesImpl(proxyActor: ActorRef) extends StoredQueriesGrpc.StoredQueries {

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def create(request: CreateStoredQuery): Future[CreateReply] = {
    ask(proxyActor, request).mapTo[CreateReply]
  }
}
