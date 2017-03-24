package com.socialthingy.plusf.sound

import com.jsyn.data.FloatSample
import com.jsyn.unitgen._

import scala.collection.mutable.ListBuffer

class Beeper(sampler: VariableRateMonoReader) {
  val updatePeriod: Double = 3500000.0 / sampler.getFrameRate

  private val beeperStates = ListBuffer[Float]()
  private var muted = false

  def mute(): Unit = {
    beeperStates.clear()
    muted = true
  }

  def unmute(): Unit = {
    muted = false
  }

  def update(state: Boolean): Unit = if (!muted) {
    beeperStates.append(if (state) 1.0F else 0.0F)
  }

  def play(): Unit = beeperStates.headOption match {
    case Some(state) =>
      if (muted || beeperStates.forall(_ == state)) {
        sampler.dataQueue.clear()
      } else {
        val sample = new FloatSample(beeperStates.toArray)
        sampler.dataQueue.queue(sample)
      }
      beeperStates.clear()

    case None =>
      sampler.dataQueue.clear()
  }
}
