package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen.*


class SoundSystem {
    val frameRate: Double = 44100.0
    val synth = JSyn.createSynthesizer()
    val lineOut = LineOut()
    val masterMixer = FourWayFade()

    private val beepSampler = createBeepSampler()

    private fun createBeepSampler(): VariableRateMonoReader {
        val s = VariableRateMonoReader()
        s.rate.set(frameRate)
        s.output.connect(0, masterMixer.input, 0)
        synth.add(s)
        return s
    }

    private val random = java.util.Random()

    private val toneChannels = createToneChannels()

    private fun createToneChannels(): List<ToneChannel> {
        return (1..3).map { chanId ->
            val tone = SquareOscillator()
            val noise = FunctionOscillator()
            noise.function.set { _ ->
                if (noise.frequency.get() == 0.0) 0.0 else -1.0 + (random.nextDouble() * 2.0)
            }
            val internalMixer = Add()

            tone.output.connect(0, internalMixer.inputA, 0)
            noise.output.connect(0, internalMixer.inputB, 0)
            internalMixer.output.connect(0, masterMixer.input, chanId)

            synth.add(tone)
            synth.add(noise)
            synth.add(internalMixer)

            ToneChannel(tone, noise)
        }
    }

    val beeper = Beeper(beepSampler)
    val ayChip = AYChip(toneChannels)

    init {
        synth.add(lineOut)
        synth.add(masterMixer)
        masterMixer.output.connect(0, lineOut.input, 0)
        masterMixer.output.connect(0, lineOut.input, 1)
    }

    private var isEnabled = false

    fun setEnabled(enabled: Boolean): Boolean {
        val wasEnabled = isEnabled
        isEnabled = enabled
        lineOut.isEnabled = enabled
        beeper.setEnabled(enabled)
        return wasEnabled
    }

    fun start() {
        synth.start(frameRate.toInt())
        lineOut.start()
    }
}
