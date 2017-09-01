package com.grandsys.inu.storedq.services

import com.grandsys.inu.storedq.commands.CreateStoredQuery
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

class StoredQueriesServer(ec: ExecutionContext) { self =>

  private val log = com.typesafe.scalalogging.Logger(this.getClass)
  private[this] var server: Server = null

  def start(port: Option[Int] = None): Unit = {
    if(server == null) {
      val serviceImpl = StoredQueriesGrpc.bindService(new StoredQueriesImpl, ec)
      server = ServerBuilder.forPort(port.getOrElse(50051)).addService(serviceImpl).build().start()
      log.info(s"Server started, listening on ${server.getPort}")
      sys.addShutdownHook {
        self.stop()
      }
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private class StoredQueriesImpl extends StoredQueriesGrpc.StoredQueries {
    override def create(request: CreateStoredQuery): Future[CreateReply] = {
      val reply = CreateReply(id = request.id, message = request.title)
      Future.successful(reply)
    }
  }
}