package com.socialthingy.plusf.p2p.discovery

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.TestKit
import akka.util.{ByteString, ByteStringBuilder}
import com.socialthingy.plusf.p2p._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps
import org.mockito.Mockito._

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
    override def deserialise(bytes: ByteString): Any = bytes.decodeString("UTF-8")
  }
  val ser = new Serialiser {
    override def serialise(obj: Any, byteStringBuilder: ByteStringBuilder): Unit =
      byteStringBuilder.append(ByteString(obj.asInstanceOf[String]))
  }

  val discoveryAddr = new InetSocketAddress("localhost", 7084)
  val discoveryActor = DiscoveryActor(discoveryAddr)

  override def afterAll(): Unit = {
    discoveryActor ! PoisonPill
  }

  "two peers in direct connect mode" should "be able to discover one another and exchange data" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1 ! Register("sess123")
    peer2 ! Register("sess123")

    try {
      eventually {
        verify(cb1).connectedToPeer(7082)
        verify(cb2).connectedToPeer(7081)
      }

      peer1 ! RawData("hello from 1")
      peer2 ! RawData("hello from 2")
      peer1 ! RawData("blah!")
      peer2 ! RawData("wibble")

      eventually {
        verify(cb1).data("hello from 2")
        verify(cb2).data("hello from 1")
        verify(cb1).data("wibble")
        verify(cb2).data("blah!")
      }
    } finally {
      peer1 ! Close
      peer2 ! Close
    }
  }

  "a peer" should "successfully reinitialise after being closed" in withPeers { (peer, cb, _, _) =>
    peer ! Register("sess123")

    eventually {
      verify(cb).waitingForPeer()
    }

    peer ! Close

    eventually {
      verify(cb).closed()
    }

    peer ! Register("sess234")

    eventually {
      verify(cb, times(2)).waitingForPeer()
    }
  }

  "a peer" should "time out its session when nobody joins" in withPeers { (lonelyPeer, cb, _, _) =>
    lonelyPeer ! Register("sess999")
    Thread.sleep(11000)
    eventually {
      verify(cb).discoveryTimeout()
    }
  }

  "a peer" should "be able to cancel its session" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1 ! Register("sess123")
    eventually {
      verify(cb1).waitingForPeer()
    }

    peer1 ! Cancel
    eventually {
      verify(cb1).discoveryCancelled()
    }

    peer2 ! Register("sess123")
    eventually {
      verify(cb2).waitingForPeer()
    }

    peer1 ! Register("sess123")

    eventually {
      verify(cb1).connectedToPeer(7082)
      verify(cb2).connectedToPeer(7081)
    }
  }

  "a peer in port forward mode" should "have its forwarded port number given to the other peer" in withPeers { (peer1, cb1, peer2, cb2) =>
    peer1 ! Register("sess123", 9876)
    peer2 ! Register("sess123")

    try {
      eventually {
        verify(cb2).connectedToPeer(9876)
      }
    } finally {
      peer1 ! Close
      peer2 ! Close
    }
  }

  def withPeers(testCode: (ActorRef, Callbacks, ActorRef, Callbacks) => Any): Unit = {
    val cb1 = mock[Callbacks]
    val peer1 = Peer(new InetSocketAddress("localhost", 7081), discoveryAddr, cb1, ser, deser, 10 seconds)

    val cb2 = mock[Callbacks]
    val peer2 = Peer(new InetSocketAddress("localhost", 7082), discoveryAddr, cb2, ser, deser, 10 seconds)

    try {
      testCode(peer1, cb1, peer2, cb2)
    } finally {
      peer1 ! Close
      peer2 ! Close
      Thread.sleep(1000)
    }
  }
}
