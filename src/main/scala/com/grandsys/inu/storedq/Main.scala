package com.grandsys.inu.storedq

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.grandsys.cluster.ActorSystemExtensions._
import com.grandsys.cluster.{ClusterConfig, SingletonProps}
import com.grandsys.inu.storedq.services.GrpcServer._
import com.grandsys.inu.storedq.services._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

object Main extends App {

  val log = com.typesafe.scalalogging.Logger(this.getClass)

  private val config: Config = ConfigFactory.load()
  private val clusterCfg = new ClusterConfig(config.getConfig("cluster"))

  log.debug(s"akka.remote.netty.tcp.port = ${config.getInt("akka.remote.netty.tcp.port")}")
  log.debug(s"akka.remote.netty.tcp.hostname = ${config.getString("akka.remote.netty.tcp.hostname")}")

  implicit val system = ActorSystem(clusterCfg.clusterName)
  implicit val ec = system.dispatcher

  val cluster: Cluster = Cluster(system)
  cluster.joinSeedNodes(clusterCfg.addresses)

  val clusterHttpMan = ClusterHttpManagement(cluster)
  clusterHttpMan.start().onComplete { _ =>
    log.info("ClusterHttpManagement is up")
    sys.addShutdownHook {
      clusterHttpMan.stop().onComplete { _ =>
        log.info("ClusterHttpManagement stopped.")
      }
    }
  }

  sys.addShutdownHook {
    // gracefullyLeaveCluster
    cluster.leave(cluster.selfAddress)
    cluster.down(cluster.selfAddress)

    system.waitTerminated()
  }

  cluster.registerOnMemberUp {

    val aggRoot = new SingletonProps(s"${StoredQueryRepoAggRoot.name}Guardian", StoredQueryRepoAggRoot.propsWithBackoff, "domain")

    system.actorOf(aggRoot.props(), name = aggRoot.name)

    val serviceImpl = new StoredQueriesImpl(system.actorOf(aggRoot.proxy()))

    StoredQueriesGrpc.bindService(serviceImpl, ExecutionContext.global).initiate().start(50051)

  }

}
