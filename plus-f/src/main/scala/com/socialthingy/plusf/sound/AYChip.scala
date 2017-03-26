package com.socialthingy.plusf.sound

import com.jsyn.unitgen._
import com.socialthingy.plusf.z80.IO

class AYChip(toneChannels: Seq[ToneChannel]) extends IO {
  private val amplitudeModeFlag = 1 << 4

  private val noiseCFlag = 1 << 5
  private val noiseBFlag = 1 << 4
  private val noiseAFlag = 1 << 3

  private val toneCFlag = 1 << 2
  private val toneBFlag = 1 << 1
  private val toneAFlag = 1

  private val ioaFlag = 1 << 6

  private var selectedRegister = 0

  private val registers = Array.ofDim[Int](18)

  def setPeriod(channel: Int, period: Int): Double = {
    toneChannels(channel).setTonePeriod(period)
  }

  override def recognises(low: Int, high: Int): Boolean = (high & 128) != 0 && (low & 2) == 0

  override def read(low: Int, high: Int): Int = registers(selectedRegister)

  override def write(low: Int, high: Int, value: Int): Unit =
    if ((high & 64) != 0) selectRegister(value & 0xf) else writeRegister(value)

  private def selectRegister(register: Int): Unit = selectedRegister = register

  private def writeRegister(value: Int): Unit = if (registers(selectedRegister) != value) {
    registers(selectedRegister) = value

    selectedRegister match {
      case r if r < 6 =>
        val channel = r / 2
        val lowReg = channel * 2
        val highReg = lowReg + 1
        setPeriod(channel, (registers(highReg) << 8) + registers(lowReg))

      case 6 =>
        val period = value & 31
        toneChannels.foreach(_.setNoisePeriod(period))

      case 7 =>
        val znoiseA = (value & noiseAFlag) == 0
        val ztoneA = (value & toneAFlag) == 0
        val znoiseB = (value & noiseBFlag) == 0
        val ztoneB = (value & toneBFlag) == 0
        val znoiseC = (value & noiseCFlag) == 0
        val ztoneC = (value & toneCFlag) == 0


        toneChannels(0).enable(ztoneA, znoiseA)
        toneChannels(1).enable(ztoneB, znoiseB)
        toneChannels(2).enable(ztoneC, znoiseC)

        val ioa = if ((value & ioaFlag) == 0) "input" else "output"

      case r if r == 8 || r == 9 || r == 10 =>
        val channel = r - 8
        if ((value & amplitudeModeFlag) == 0) {
          toneChannels(channel).setAmplitude(value & 15)
        }

      case 11 =>
        val envelopePeriod = (registers(12) << 8) + registers(11)
        val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)

      case 12 =>
        val envelopePeriod = (registers(12) << 8) + registers(11)
        val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)

      case 13 =>

      case 15 =>

      case 14 =>
    }
  }
}

class ToneChannel(tone: SquareOscillator, noise: FunctionOscillator) {
  tone.amplitude.set(1.0)
  tone.frequency.set(0.0)
  noise.amplitude.set(1.0)
  noise.frequency.set(0.0)

  def enable(toneChannel: Boolean, noiseChannel: Boolean): Unit = {
    tone.setEnabled(toneChannel)
    noise.setEnabled(noiseChannel)
  }

  private val amplitudeStep = 1.0 / 15

  def setAmplitude(amplitude: Int) = {
    noise.amplitude.set(amplitudeStep * amplitude)
    tone.amplitude.set(amplitudeStep * amplitude)
  }

  def setTonePeriod(period: Int) = {
    val frequency = periodToFrequency(period)
    tone.frequency.set(frequency)
    frequency
  }

  def setNoisePeriod(period: Int) = {
    val frequency = periodToFrequency(period)
    noise.frequency.set(frequency)
    frequency
  }

  private def periodToFrequency(period: Int): Double = if (period == 0) {
    0.0
  } else {
    3546900.0 / (16 * period)
  }
}
