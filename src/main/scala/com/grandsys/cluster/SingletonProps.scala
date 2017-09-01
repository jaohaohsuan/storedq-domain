package com.grandsys.cluster

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

class SingletonProps(val name: String, private val singletonProps: Props, val role: String) {

  val proxyName = s"${name}Proxy"

  def props()(implicit system: ActorSystem): Props = {
    ClusterSingletonManager.props(
      singletonProps = singletonProps,
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system).withRole(role)
    )
  }

  def proxy()(implicit system: ActorSystem): Props = {
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/$name",
      settings = ClusterSingletonProxySettings(system).withRole(role)
    )
  }

}
