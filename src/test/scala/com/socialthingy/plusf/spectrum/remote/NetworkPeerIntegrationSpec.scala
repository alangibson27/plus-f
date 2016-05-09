package com.socialthingy.plusf.spectrum.remote

import java.lang.Long
import java.net.{InetSocketAddress, SocketAddress}
import java.util.function.{Consumer, Supplier}

import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

class NetworkPeerIntegrationSpec extends FlatSpec with Matchers with Eventually with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(5 seconds)

  var leftConsumed: ListBuffer[RightData] = null
  var leftPeer: NetworkPeer[RightData] = null
  var rightConsumed: ListBuffer[LeftData] = null
  var rightPeer: NetworkPeer[LeftData] = null

  before {
    val left = aNetworkPeer[RightData](7000, new InetSocketAddress("localhost", 7001))
    leftConsumed = left._1
    leftPeer = left._2

    val right = aNetworkPeer[LeftData](7001, new InetSocketAddress("localhost", 7000))
    rightConsumed = right._1
    rightPeer = right._2
  }

  after {
    leftPeer.disconnect()
    rightPeer.disconnect()
  }

  "two network peers" should "communicate with one another successfully locally" in {
    // when
    leftPeer.sendDataToPartner(LeftData(1))
    rightPeer.sendDataToPartner(RightData(1))

    // then
    eventually {
      withClue("left peer") {
        leftConsumed should have size 1
        leftConsumed should contain(RightData(1))
      }

      withClue("right peer") {
        rightConsumed should have size 1
        rightConsumed should contain(LeftData(1))
      }
    }
  }

  private val realTimestamper = new Supplier[java.lang.Long] {
    override def get(): Long = System.currentTimeMillis()
  }

  private def aNetworkPeer[R <: java.io.Serializable]
    (port: Int, partnerAddress: SocketAddress) = {
    val consumed = ListBuffer[R]()
    val consumer = new Consumer[R] {
      override def accept(t: R): Unit = consumed += t
    }
    val peer = new NetworkPeer[R](consumer, realTimestamper, port, partnerAddress)

    (consumed, peer)
  }
}

case class RightData(data: Int) extends java.io.Serializable
case class LeftData(data: Int) extends java.io.Serializable
