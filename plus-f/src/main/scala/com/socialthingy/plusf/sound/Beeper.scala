package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen.{LineOut, SquareOscillator}

import scala.annotation.tailrec
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

  def beep(tstate: Int): Unit = beepTstates.append(tstate)

  def play: Double = if (beepTstates.size < 2) {
    osc.noteOff()
    0.0
  } else {
      val tones = Tones.fromBeeps(beepTstates.toList)
      val now = synth.createTimeStamp()
      var offset: Double = 0.0
      tones foreach { t =>
        osc.noteOn(t.frequency, 0.8, now.makeRelative(offset))
        offset = offset + t.duration
      }

      beepTstates.clear()
      tones.head.frequency
  }
}

object Tones {
  val clockFrequency: Double = 3500000.0
  val tstateDurationSecs: Double = 1 / clockFrequency

  def fromBeeps(beepTstates: List[Int]): List[Tone] = {
    @tailrec
    def loop(intervals: List[Int], acc: List[Tone], currentTone: Tone): List[Tone] = intervals match {
      case thisInterval :: t if thisInterval == currentTone.interval =>
        loop(t, acc, currentTone.extended)

      case thisInterval :: t if thisInterval != currentTone.interval =>
        loop(t, currentTone :: acc, Tone(thisInterval, thisInterval))

      case Nil => (currentTone :: acc).reverse
    }

    val intervals = if (beepTstates.size == 1) {
      beepTstates
    } else {
      beepTstates zip beepTstates.tail map { case (lo, hi) => hi - lo }
    }
    val firstInterval = intervals.head
    loop(intervals.tail, List(), Tone(firstInterval, firstInterval))
  }
}

case class Tone(interval: Int, tstates: Int) {
  import Tones._
  def extended: Tone = copy(interval, tstates + interval)
  def frequency: Double = (1.0 / (interval / clockFrequency)) / 2.0
  def duration: Double = tstates * tstateDurationSecs
}

object Test extends App {
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

  (0 until 1000) foreach { i =>
    osc.noteOn(if (i % 2 == 0) 261.0 else 261.0 * 2, 0.8, synth.createTimeStamp.makeRelative(0.001 * i))
  }
  osc.noteOff(synth.createTimeStamp().makeRelative(1.0))

  Thread.sleep(10000)
  osc.stop()
  lineOut.stop()
  synth.stop()
}
