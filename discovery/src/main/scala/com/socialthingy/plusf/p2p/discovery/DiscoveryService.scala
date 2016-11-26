package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress

import akka.actor.ActorSystem

object DiscoveryService extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val port = args(0).toInt
  val actor = DiscoveryActor(new InetSocketAddress(port))

  system.log.info(s"DiscoveryService listening on port $port")
}
