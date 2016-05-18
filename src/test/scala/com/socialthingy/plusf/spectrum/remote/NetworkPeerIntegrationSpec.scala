package com.socialthingy.plusf.spectrum.remote

import java.io.{InputStream, OutputStream}
import java.lang.Long
import java.net.{InetSocketAddress, SocketAddress}
import java.util.function.{Consumer, Supplier, Function => JFunction}

import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

import org.apache.commons.lang3.tuple.{Pair => JPair}

class NetworkPeerIntegrationSpec extends FlatSpec with Matchers with Eventually with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(5 seconds)

  var leftConsumed: ListBuffer[RightData] = null
  var leftPeer: NetworkPeer[RightData, LeftData] = null
  var rightConsumed: ListBuffer[LeftData] = null
  var rightPeer: NetworkPeer[LeftData, RightData] = null

  before {
    val left = aNetworkPeer[RightData, LeftData](7000,
      new InetSocketAddress("localhost", 7001),
      LeftData.serialiser,
      RightData.deserialiser
    )
    leftConsumed = left._1
    leftPeer = left._2

    val right = aNetworkPeer[LeftData, RightData](7001,
      new InetSocketAddress("localhost", 7000),
      RightData.serialiser,
      LeftData.deserialiser
    )
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

  private def aNetworkPeer[R,S](port: Int,
                                partnerAddress: SocketAddress,
                                serialiser: Consumer[JPair[S, OutputStream]],
                                deserialiser: JFunction[InputStream, R]) = {
    val consumed = ListBuffer[R]()
    val consumer = new Consumer[R] {
      override def accept(t: R): Unit = consumed += t
    }
    val peer = new NetworkPeer[R,S](consumer, serialiser, deserialiser, realTimestamper, port, partnerAddress)

    (consumed, peer)
  }
}

object RightData {
  val serialiser: Consumer[JPair[RightData, OutputStream]] = new Consumer[JPair[RightData, OutputStream]] {
    override def accept(t: JPair[RightData, OutputStream]): Unit = t.getValue.write(t.getKey.data)
  }

  val deserialiser: JFunction[InputStream, RightData] = new JFunction[InputStream, RightData] {
    override def apply(t: InputStream): RightData = RightData(t.read())
  }
}
case class RightData(data: Int)

object LeftData {
  val serialiser: Consumer[JPair[LeftData, OutputStream]] = new Consumer[JPair[LeftData, OutputStream]] {
    override def accept(t: JPair[LeftData, OutputStream]): Unit = t.getValue.write(t.getKey.data)
  }

  val deserialiser: JFunction[InputStream, LeftData] = new JFunction[InputStream, LeftData] {
    override def apply(t: InputStream): LeftData = LeftData(t.read())
  }
}
case class LeftData(data: Int)
