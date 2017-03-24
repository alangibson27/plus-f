package com.socialthingy.plusf.sound

import com.jsyn.unitgen._

class AYChip(toneChannels: Seq[ToneChannel]) {
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

  def selectRegister(register: Int): Unit = selectedRegister = register

  def setPeriod(channel: Int, period: Int): Unit = {
    val frequency = toneChannels(channel).setTonePeriod(period)
    println(s"set channel $channel to $frequency hz")
  }

  def read: Int = registers(selectedRegister)

  def write(value: Int): Unit = if (registers(selectedRegister) != value) {
    registers(selectedRegister) = value

    selectedRegister match {
      case r if r < 6 =>
        val channel = r / 2
        val lowReg = channel * 2
        val highReg = lowReg + 1
        setPeriod(channel, (registers(highReg) << 8) + registers(lowReg))

      case 6 =>
        println(s"Set noise period to $value")
        val period = value & 31
        toneChannels.foreach(_.setNoisePeriod(period))

      case 7 =>
        val znoiseA = (value & noiseAFlag) == 0
        val ztoneA = (value & toneAFlag) == 0
        val znoiseB = (value & noiseBFlag) == 0
        val ztoneB = (value & toneBFlag) == 0
        val znoiseC = (value & noiseCFlag) == 0
        val ztoneC = (value & toneCFlag) == 0

        println(s"Noise enabled on ${if (znoiseA) "A" else ""}${if (znoiseB) "B" else ""}${if (znoiseC) "C" else ""}")
        println(s"Tone enabled on ${if (ztoneA) "A" else ""}${if (ztoneB) "B" else ""}${if (ztoneC) "C" else ""}")

        toneChannels(0).enable(ztoneA, znoiseA)
        toneChannels(1).enable(ztoneB, znoiseB)
        toneChannels(2).enable(ztoneC, znoiseC)

        val ioa = if ((value & ioaFlag) == 0) "input" else "output"
        println(s"IOA set to $ioa")

      case r if r == 8 || r == 9 || r == 10 =>
        val channel = r - 8
        if ((value & amplitudeModeFlag) == 0) {
          toneChannels(channel).setAmplitude(value & 15)
          println(s"Setting amplitude $channel to $value")
        } else {
          println(s"Setting amplitude $channel to envelope")
        }

      case 11 =>
        val envelopePeriod = (registers(12) << 8) + registers(11)
        val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)
        println(s"Setting envelope frequency to $envelopeFrequency hz")

      case 12 =>
        val envelopePeriod = (registers(12) << 8) + registers(11)
        val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)
        println(s"Setting envelope frequency to $envelopeFrequency hz")

      case 13 =>
        println(s"Setting envelope pattern to $value")

      case 15 =>
        println("oops!")

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