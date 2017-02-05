package com.socialthingy.plusf.spectrum

import java.nio.ByteBuffer
import javax.sound.sampled._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Sound extends App {
  val sampleRate: Float = 8000
  val af = new AudioFormat(sampleRate, 8, 1, true, false)
  val sdl = AudioSystem.getSourceDataLine(af)
  sdl.open(af)
  sdl.start()

  sound(261, 1000, 0.8)

  sdl.drain()
  sdl.close()

  def sound(hz: Int, msecs: Int, vol: Double) = {
    val samples = sampleRate.toInt * msecs / 1000
    val angles = (0 until samples) map ( _ / (sampleRate / hz) * 2.0 * Math.PI)
    val buf = angles map { angle =>
      (Math.sin(angle) * 127.0 * vol).toByte
    }

    sdl.write(buf.toArray, 0, buf.size)
  }

  /*
   * Assume fixed sample rate of 8000/s
   * If frequency = 261, advance in steps of (8000 / 261 =) 30
   * If frequency = 300, advance in steps of (8000 / 300 =) 26
   */
}

object Beeper extends App {
  val beeper = new Beeper(20, 8000)

  beeper.beep(261.3)
  Thread.sleep(2000)
  beeper.beep(400)

  beeper.close()
}

class Beeper(durationMs: Int, sampleRate: Int = 32000) {
  val sampleSize = sampleRate * durationMs / 1000
  val buf = ByteBuffer.allocate(sampleSize)
  val af = new AudioFormat(sampleRate, 8, 1, true, false)
  val sdl = AudioSystem.getSourceDataLine(af)
  sdl.open(af)
  sdl.start()

  val f = Future {
    val arr = Array.ofDim[Byte](1)
    while (true) {
      arr(0) = (Math.sin(currentAngle) * 127.0).toByte
      currentAngle = currentAngle + currentIncrement
      sdl.write(arr, 0, 1)
    }
  }

  private var currentIncrement: Double = 0
  private var currentAngle: Double = 0

  def beep(frequencyHz: Double): Unit = {
    currentIncrement = 1.0 / (sampleRate / frequencyHz) * 2.0 * Math.PI
  }

  def close(): Unit = {
    sdl.drain()
    sdl.close()
  }
}