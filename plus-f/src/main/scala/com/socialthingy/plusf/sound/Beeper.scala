package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen.{LineOut, SquareOscillator}

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

  private var firstBeep: Option[Int] = None
  private var lastBeep: Option[Int] = None
  private var beeps: Int = 0

  def beep(timeMs: Int): Unit = {
    beeps = beeps + 1

    (firstBeep, lastBeep) match {
      case (None, _) => firstBeep = Some(timeMs)
      case (Some(_), _) => lastBeep = Some(timeMs)
    }
  }

  def play: Double = (firstBeep, lastBeep) match {
    case (None, _) =>
      osc.setEnabled(false)
      0.0

    case (_, None) =>
      osc.setEnabled(false)
      0.0

    case (Some(f), Some(l)) =>
      if (!osc.isEnabled) {
        osc.setEnabled(true)
      }

      val intervalMs = l - f
      val frequency = ((beeps - 1) * (1000.0 / intervalMs)) / 2
      osc.frequency.set(frequency)
      firstBeep = None
      lastBeep = None
      beeps = 0
      frequency
  }
}