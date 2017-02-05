package com.socialthingy.plusf.sound

import java.nio.ByteBuffer
import java.util.concurrent.{Executors, TimeUnit}
import javax.sound.sampled.{AudioFormat, AudioSystem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Beeper(sampleRate: Int = 32000) {
  val buf = ByteBuffer.allocate(1024)
  val af = new AudioFormat(sampleRate, 8, 1, true, false)
  val sdl = AudioSystem.getSourceDataLine(af)
  sdl.open(af)
  sdl.start()

  private var currentIncrement: Double = 0
  private var currentAngle: Double = 0
  private val sampleSize = sampleRate * 5 / 1000
  private var currentFrequency: java.lang.Double = 0.0

  val f = Future {
    val arr = Array.ofDim[Byte](sampleSize)
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() => {
      (0 until sampleSize) foreach { i =>
        arr(i) = (Math.sin(currentAngle) * 127.0).toByte
        currentAngle = currentAngle + currentIncrement
      }
      sdl.write(arr, 0, sampleSize)
    }, 0, 5, TimeUnit.MILLISECONDS)
  }

  def beep(frequencyHz: java.lang.Double): Unit = {
    if (!sdl.isRunning) {
      sdl.start()
    }

    if (frequencyHz != currentFrequency) {
      sdl.flush()
      currentIncrement = 1.0 / (sampleRate / frequencyHz) * 2.0 * Math.PI
      currentFrequency = frequencyHz
    }
  }

  def off(): Unit = {
    sdl.stop()
  }

  def close(): Unit = {
    sdl.drain()
    sdl.close()
  }
}