package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen._

class SoundSystem {
  val frameRate = 44100
  val synth = JSyn.createSynthesizer
  val lineOut = new LineOut
  val masterMixer = new FourWayFade

  private val beepSampler = {
    val s = new VariableRateMonoReader
    s.rate.set(frameRate)
    s.output.connect(0, masterMixer.input, 0)
    synth.add(s)
    s
  }

  private val toneChannels = (1 until 4) map { chanId =>
    val tone = new SquareOscillator
    val noise = new FunctionOscillator
    noise.function.set((v: Double) => {
        if (noise.frequency.get() == 0.0) 0.0 else -1.0 + (scala.util.Random.nextDouble() * 2.0)
    })
    val internalMixer = new Add

    tone.output.connect(0, internalMixer.inputA, 0)
    noise.output.connect(0, internalMixer.inputB, 0)
    internalMixer.output.connect(0, masterMixer.input, chanId)

    synth.add(tone)
    synth.add(noise)
    synth.add(internalMixer)

    new ToneChannel(tone, noise)
  }

  val beeper = new Beeper(beepSampler)
  val ayChip = new AYChip(toneChannels)

  synth.add(lineOut)
  synth.add(masterMixer)
  masterMixer.output.connect(0, lineOut.input, 0)
  masterMixer.output.connect(0, lineOut.input, 1)

  private var isEnabled = false

  def setEnabled(enabled: Boolean): Boolean = {
    val wasEnabled = isEnabled
    isEnabled = enabled
    lineOut.setEnabled(enabled)
    beeper.setEnabled(enabled)
    wasEnabled
  }

  def start(): Unit = {
    synth.start(frameRate)
    lineOut.start()
  }
}
