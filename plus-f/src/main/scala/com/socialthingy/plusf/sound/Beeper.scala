package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.data.FloatSample
import com.jsyn.unitgen._

import scala.collection.mutable.ListBuffer

class Beeper {
  private val synth = JSyn.createSynthesizer
  synth.start(44100)

  private val sampler = new VariableRateMonoReader
  private val lineOut = new LineOut
  private val filter = new FilterLowPass

  synth.add(sampler)
  synth.add(lineOut)
  synth.add(filter)

  sampler.amplitude.set(0.8)
  sampler.rate.set(synth.getFrameRate)
  sampler.output.connect(0, lineOut.input, 0)
  sampler.output.connect(0, lineOut.input, 1)

  sampler.start()
  filter.start()
  lineOut.start()

  val updatePeriod: Double = 3500000.0 / synth.getFrameRate
  private val beeperStates = ListBuffer[Float]()
  private var enabled = false

  def enable(enabled: Boolean): Unit = this.enabled = enabled

  def update(state: Boolean): Unit = beeperStates.append(if (state) 1.0F else 0.0F)

  def play(): Unit = {
    val first = beeperStates.head
    if (!enabled || beeperStates.forall(_ == first)) {
      sampler.dataQueue.clear()
    } else {
      val sample = new FloatSample(beeperStates.toArray)
      sampler.dataQueue.queue(sample)
    }
    beeperStates.clear()
  }

  def discard(): Unit = beeperStates.clear()
}
