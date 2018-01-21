package com.socialthingy.plusf.sound

import com.jsyn.data.FloatSample
import com.jsyn.unitgen._

import scala.collection.mutable.ListBuffer

class Beeper(sampler: VariableRateMonoReader) {
  val updatePeriod: Double = 3500000.0 / sampler.rate.get()

  private val beeperStates = ListBuffer[Float]()
  private var isEnabled = false

  def setEnabled(enabled: Boolean): Unit = {
    isEnabled = enabled

    if (!enabled) {
      beeperStates.clear()
    }
  }

  def update(state: Boolean): Unit = if (isEnabled) {
    beeperStates.append(if (state) 1.0F else 0.0F)
  }

  def play(): Unit = beeperStates.headOption match {
    case Some(state) =>
      if (!isEnabled || beeperStates.forall(_ == state)) {
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
