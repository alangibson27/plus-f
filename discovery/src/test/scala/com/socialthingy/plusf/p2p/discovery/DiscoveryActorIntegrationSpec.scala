package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.Optional

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestKit
import com.socialthingy.p2p.{Peer, _}
import com.socialthingy.plusf.p2p._
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

class DiscoveryActorIntegrationSpec
  extends TestKit(ActorSystem())
    with FlatSpecLike
    with Matchers
    with Eventually
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfterAll {
  implicit val patience = PatienceConfig(10 seconds)
  implicit val ec = system.dispatcher

  val deser = new Deserialiser {
    override def deserialise(bytes: ByteBuffer): Object =
      new String(bytes.array(), bytes.position(), bytes.remaining(), "UTF-8")
  }
  val ser = new Serialiser {
    override def serialise(obj: Object, byteStringBuilder: ByteBuffer): Unit = {
      byteStringBuilder.put(obj.asInstanceOf[String].getBytes("UTF-8"))
    }
  }

  val discoveryAddr = new InetSocketAddress("localhost", 7084)
  val discoveryActor = DiscoveryActor(discoveryAddr)

  override def afterAll(): Unit = {
    discoveryActor ! PoisonPill
  }

  "two peers in direct connect mode" should "be able to discover one another and exchange data" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1.join("sess123", Optional.empty())
    peer2.join("sess123", Optional.empty())

    eventually {
      verify(cb1).connectedToPeer(7082)
      verify(cb2).connectedToPeer(7081)
    }

    peer1.send(new RawData("hello from 1", 0))
    peer2.send(new RawData("hello from 2", 0))
    peer1.send(new RawData("blah!", 0))
    peer2.send(new RawData("wibble", 0))

    eventually {
      verify(cb1).data("hello from 2")
      verify(cb2).data("hello from 1")
      verify(cb1).data("wibble")
      verify(cb2).data("blah!")
    }
  }

  "a peer" should "successfully reinitialise after being closed" in withPeers { (peer, cb, _, _) =>
    peer.join("sess123", Optional.empty())

    eventually {
      verify(cb).waitingForPeer()
    }

    peer.close()

    eventually {
      verify(cb).discoveryCancelled()
    }

    peer.join("sess234", Optional.empty())

    eventually {
      verify(cb, times(2)).waitingForPeer()
    }
  }

  "a peer" should "time out its session when nobody joins" in withPeers { (lonelyPeer, cb, _, _) =>
    lonelyPeer.join("sess999", Optional.empty())
    Thread.sleep(11000)
    eventually {
      verify(cb).discoveryTimeout()
    }
  }

  "a peer" should "be able to cancel its session before a peer joins" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1.join("sess123", Optional.empty())
    eventually {
      verify(cb1).waitingForPeer()
    }

    peer1.close()
    eventually {
      verify(cb1).discoveryCancelled()
    }

    peer2.join("sess123", Optional.empty())
    eventually {
      verify(cb2).waitingForPeer()
    }

    peer1.join("sess123", Optional.empty())

    eventually {
      verify(cb1).connectedToPeer(7082)
      verify(cb2).connectedToPeer(7081)
    }
  }

  "a peer in port forward mode" should "have its forwarded port number given to the other peer" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1.join("sess123", Optional.of(9876))
    peer2.join("sess123", Optional.empty())

    eventually {
      verify(cb2).connectedToPeer(9876)
    }
  }

  def withPeers(testCode: (Peer, Callbacks, Peer, Callbacks) => Any): Unit = {
    val cb1 = mock[Callbacks]
    val peer1 = new Peer(new InetSocketAddress("localhost", 7081), discoveryAddr, cb1, ser, deser, java.time.Duration.ofSeconds(10))

    val cb2 = mock[Callbacks]
    val peer2 = new Peer(new InetSocketAddress("localhost", 7082), discoveryAddr, cb2, ser, deser, java.time.Duration.ofSeconds(10))

    try {
      testCode(peer1, cb1, peer2, cb2)
    } finally {
      peer1.close()
      peer2.close()
      Thread.sleep(2000)
    }
  }
}
