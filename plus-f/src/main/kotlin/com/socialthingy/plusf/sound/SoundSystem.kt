package com.socialthingy.plusf.sound

import com.jsyn.JSyn
import com.jsyn.unitgen.*

const val frameRate = 44100

class SoundSystem {
    private val synth = JSyn.createSynthesizer()!!
    private val lineOut = LineOut()
    private val masterMixer = FourWayFade()

    private val random = java.util.Random()
    private val beepSampler = createBeepSampler()

    private fun createBeepSampler(): VariableRateMonoReader {
        val s = VariableRateMonoReader()
        s.rate.set(frameRate.toDouble())
        s.output.connect(0, masterMixer.input, 0)
        synth.add(s)
        return s
    }

    private val toneChannels = createToneChannels()

    private fun createToneChannels(): List<ToneChannel> {
        val toneChannels = mutableListOf<ToneChannel>()
        for (i in 1..3) {
            val tone = SquareOscillator()
            val noise = FunctionOscillator()

            noise.function.set {
                if (noise.frequency.get() == 0.0) 0.0 else -1.0 + (random.nextDouble() * 2.0)
            }

            val internalMixer = Add()

            tone.output.connect(0, internalMixer.inputA, 0)
            noise.output.connect(0, internalMixer.inputB, 0)
            internalMixer.output.connect(0, masterMixer.input, i)

            synth.add(tone)
            synth.add(noise)
            synth.add(internalMixer)

            toneChannels.add(ToneChannel(tone, noise))
        }

        return toneChannels
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
        synth.start(frameRate)
        lineOut.start()
    }
}
