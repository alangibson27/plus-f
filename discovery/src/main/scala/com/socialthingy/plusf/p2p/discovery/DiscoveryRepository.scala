package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

import akka.actor.ActorSystem

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.language.postfixOps

trait DiscoveryRepository {
  val activeSessions = TrieMap[String, Session]()
  def system: ActorSystem

  def scheduleCleanup(sessionCleanupInterval: FiniteDuration, sessionDuration: FiniteDuration): Unit = {
    implicit val ec = system.dispatcher
    system.scheduler.schedule(sessionCleanupInterval, sessionCleanupInterval) {
      val expiredSessionIds = activeSessions.filter(_._2.isTimedOut(sessionDuration)).keys
      expiredSessionIds.foreach(activeSessions.remove)
    }
  }

  def connectToSession(sessionId: String, addr: InetSocketAddress): Option[InetSocketAddress] = {
    activeSessions.get(sessionId) match {
      case Some(Session(initiator, _)) if initiator.equals(addr) =>
        None

      case Some(Session(initiator, _)) =>
        activeSessions.remove(sessionId)
        Some(initiator)

      case None =>
        activeSessions += sessionId -> Session(addr, LocalDateTime.now())
        None
    }
  }

  def cancelSession(sessionId: String, addr: InetSocketAddress): Unit = {
    activeSessions.get(sessionId)
      .filter(_.initiator.equals(addr))
      .foreach(_ => activeSessions.remove(sessionId))
  }
}

case class Session(initiator: InetSocketAddress, startTime: LocalDateTime) {
  def isTimedOut(sessionDuration: Duration) =
    LocalDateTime.now().isAfter(startTime.plus(sessionDuration))

  implicit def durationToTemporalAmount(duration: Duration): TemporalAmount =
    java.time.Duration.ofMillis(duration.toMillis)
}