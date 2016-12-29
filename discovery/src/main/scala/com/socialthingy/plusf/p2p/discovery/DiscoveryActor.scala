package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import akka.util.ByteString.UTF_8

object DiscoveryActor {
  def apply(bindAddress: InetSocketAddress)(implicit system: ActorSystem): ActorRef = {
    val actor = system.actorOf(Props(new DiscoveryActor(bindAddress)))
    actor
  }
}

class DiscoveryActor(bindAddress: InetSocketAddress) extends Actor with ActorLogging with DiscoveryRepository {
  implicit override val system = context.system
  IO(Udp) ! Udp.Bind(self, bindAddress)

  override def receive: Receive = {
    case Udp.Bound(local) =>
      log.info("Bound to {}", local)
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(content, remote) =>
      val data = content.decodeString(UTF_8)
      data.split('|').toList match {
        case "JOIN" :: (sessionId: String) :: Nil =>
          joinSession(socket, remote, sessionId)

        case "JOIN" :: (sessionId: String) :: (port: String) :: Nil =>
          joinSession(socket, new InetSocketAddress(remote.getAddress, port.toInt), sessionId)

        case "CANCEL" :: (sessionId: String) :: Nil =>
          cancelSession(sessionId, remote)

        case _ =>
          log.error(s"Unknown request $data received from $remote")
      }
  }

  def joinSession(socket: ActorRef, remote: InetSocketAddress, sessionId: String): Unit = {
    connectToSession(sessionId, remote) match {
      case None =>
        log.info("Initiating new session with ID {} from {}", sessionId, remote)
        socket ! Udp.Send(ByteString("WAIT"), remote)
      case Some(initiator) =>
        log.info("Completing new session with ID {} between {} and {}", sessionId, initiator, remote)
        socket ! Udp.Send(peerIdentifierStringFor(initiator), remote)
        socket ! Udp.Send(peerIdentifierStringFor(remote), initiator)
    }
  }

  private def peerIdentifierStringFor(addr: InetSocketAddress) =
    ByteString(s"PEER|${addr.getHostName}|${addr.getPort}")
}
