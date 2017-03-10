package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen.{LineOut, SquareOscillator}

import scala.collection.mutable.ListBuffer

class Beeper(mute: Boolean = false) {
  private val synth = JSyn.createSynthesizer
  synth.start()

  private val osc = new SquareOscillator
  private val lineOut = new LineOut
  synth.add(osc)
  synth.add(lineOut)
  osc.output.connect(0, lineOut.input, 0)
  osc.output.connect(0, lineOut.input, 1)
  osc.amplitude.set(0.8)

  osc.start()
  lineOut.start()

  private val beepTstates = ListBuffer[Int]()
//  private var firstBeep: Option[Int] = None
//  private var lastBeep: Option[Int] = None
//  private var beeps: Int = 0
//  private var prevFreq: Option[Double] = None

  def beep(tstate: Int): Unit = beepTstates.append(tstate)

  def play: Double = if (beepTstates.size < 2) {
    osc.setEnabled(false)
    0.0
  } else {
      if (!osc.isEnabled) {
        osc.setEnabled(true)
      }

      val intervals = beepTstates zip beepTstates.tail map { case (lo, hi) => hi - lo }
      val intervalTstates = intervals.groupBy(identity).toList.sortBy(_._2.size)

      val frequency = intervalTstates match {
        case i1 :: Nil =>
          setFreq(i1._1)

        case i1 :: i2 :: _ =>
          setFreq(i1._1)
          setFreq(i2._1)
      }

      beepTstates.clear()
      frequency
  }

  private val clockFrequency = 3500000.0

  private def setFreq(intervalTstates: Int): Double = {
    val period: Double = intervalTstates / clockFrequency
    val frequency: Double = (1.0 / period) / 2.0
    osc.frequency.set(frequency, 0.001)
    frequency
  }
}