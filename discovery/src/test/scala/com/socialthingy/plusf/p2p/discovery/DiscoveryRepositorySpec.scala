package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import org.scalatest.{FlatSpec, Matchers}

import scala.language.postfixOps

import scala.concurrent.duration._
import scala.language.postfixOps

class DiscoveryRepositorySpec extends FlatSpec with Matchers {
  val testAddr1 = new InetSocketAddress("localhost", 7000)
  val testAddr2 = new InetSocketAddress("localhost", 7001)

  "DiscoveryRepository" should "create a new session under a session ID it hasn't seen before" in new TestDiscoveryRepository {
    connectToSession("newId", testAddr1) shouldBe None
  }

  it should "return the session initiator's address when a new client connects to a prior session" in new TestDiscoveryRepository {
    connectToSession("newId", testAddr1)
    connectToSession("newId", testAddr2) shouldBe Some(testAddr1)
  }

  it should "do nothing when the initiator attempts to connect to a session it has already initiated" in new TestDiscoveryRepository {
    connectToSession("newId", testAddr1)
    connectToSession("newId", testAddr1) shouldBe None
  }

  it should "clean out a session once two peers have connected" in new TestDiscoveryRepository {
    connectToSession("newId", testAddr1)
    connectToSession("newId", testAddr2)

    connectToSession("newId", testAddr2) shouldBe None
  }

  it should "clean out expired sessions when scheduled cleanup takes place" in new TestDiscoveryRepository {
    scheduleCleanup(1 second, 5 seconds)

    connectToSession("newId", testAddr1) shouldBe None
    Thread.sleep(6000)
    connectToSession("newId", testAddr2) shouldBe None
  }

  it should "clean out a session when told to cancel it" in new TestDiscoveryRepository {
    connectToSession("toBeCancelled", testAddr1) shouldBe None
    cancelSession("toBeCancelled", testAddr1)
    connectToSession("toBeCancelled", testAddr2) shouldBe None
  }

  trait TestDiscoveryRepository extends DiscoveryRepository {
    val system = ActorSystem()
  }
}
