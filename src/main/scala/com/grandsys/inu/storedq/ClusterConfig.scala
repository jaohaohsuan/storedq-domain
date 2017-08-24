package com.grandsys.inu.storedq

import com.typesafe.config.Config
import akka.actor.Address
import collection.JavaConverters._

class ClusterConfig(val config: Config)  {

  val portNum = config.getInt("port")
  val clusterName = config.getString("name")

  lazy val addresses: List[Address] = {
    config.getStringList("seed-nodes").asScala.map { node => new Address("akka.tcp", clusterName, node.trim, portNum) } toList
  }
}