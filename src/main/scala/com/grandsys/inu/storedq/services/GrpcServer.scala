package com.grandsys.inu.storedq.services

import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}

object GrpcServer {
  implicit class runServer(serviceImpl: ServerServiceDefinition) {
    def initiate() = {
      new GrpcServer(serviceImpl)
    }
  }
}

class GrpcServer(serviceImpl: ServerServiceDefinition) { self =>

  private val log = com.typesafe.scalalogging.Logger(this.getClass)
  private[this] var server: Server = null

  def start(port: Int): Unit = {
    if(server == null) {
      server = ServerBuilder.forPort(port).addService(serviceImpl).build().start()
      log.info(s"Server started, listening on ${server.getPort}")
      sys.addShutdownHook {
        self.stop()
      }
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
      log.info(s"Server stopped")
    }
  }
}