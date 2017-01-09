package com.socialthingy.plusf.p2p

trait Callbacks {
  def data(content: Any): Unit

  def discoveryTimeout(): Unit
  def discoveryCancelled(): Unit
  def waitingForPeer(): Unit
  def connectedToPeer(port: Int): Unit
  def discovering(): Unit
  def initialising(): Unit
  def closed(): Unit
}
