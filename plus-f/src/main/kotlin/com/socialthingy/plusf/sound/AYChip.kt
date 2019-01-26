package com.socialthingy.plusf.sound

import com.jsyn.unitgen.*
import com.socialthingy.plusf.z80.IO

class AYChip(private val toneChannels: List<ToneChannel>): IO {
    private val amplitudeModeFlag = 1 shl 4

    private val noiseCFlag = 1 shl 5
    private val noiseBFlag = 1 shl 4
    private val noiseAFlag = 1 shl 3

    private val toneCFlag = 1 shl 2
    private val toneBFlag = 1 shl 1
    private val toneAFlag = 1

    private var selectedRegister = 0
    public var lastWriteTo0xfffd = 0
        get() = field

    private val registers = IntArray(18)

    private fun setPeriod(channel: Int, period: Int): Double {
        return toneChannels[channel].setTonePeriod(period)
    }

    override fun recognises(low: Int, high: Int): Boolean = (high and 128) != 0 && (low and 2) == 0

    override fun read(low: Int, high: Int): Int = registers[selectedRegister]

    override fun write(low: Int, high: Int, value: Int) {
        if (high == 0xff && low == 0xfd) {
            lastWriteTo0xfffd = value
        }

        if ((high and 64) != 0) selectRegister(value and 0xf) else writeRegister(value)
    }

    private fun selectRegister(register: Int) {
        selectedRegister = register
    }

    private fun writeRegister(value: Int) {
        if (registers[selectedRegister] != value) {
            registers[selectedRegister] = value

            when {
                selectedRegister < 6 -> {
                    val channel = selectedRegister / 2
                    val lowReg = channel * 2
                    val highReg = lowReg + 1
                    this.setPeriod(channel, (registers[highReg] shl 8) + registers[lowReg])
                }

                selectedRegister == 6 -> {
                    val period = value and 31
                    toneChannels.forEach { it.setNoisePeriod(period) }
                }

                selectedRegister == 7 -> {
                    val znoiseA = (value and noiseAFlag) == 0
                    val ztoneA = (value and toneAFlag) == 0
                    val znoiseB = (value and noiseBFlag) == 0
                    val ztoneB = (value and toneBFlag) == 0
                    val znoiseC = (value and noiseCFlag) == 0
                    val ztoneC = (value and toneCFlag) == 0


                    toneChannels[0].enable(ztoneA, znoiseA)
                    toneChannels[1].enable(ztoneB, znoiseB)
                    toneChannels[2].enable(ztoneC, znoiseC)
                }

                selectedRegister in 8..10 -> {
                    val channel = selectedRegister - 8
                    if ((value and amplitudeModeFlag) == 0) {
                        toneChannels[channel].setAmplitude(value and 15)
                    }
                }

                selectedRegister in 11..15 -> {}
            }
        }
    }
}

class ToneChannel(private val tone: SquareOscillator, private val noise: FunctionOscillator) {
    init {
        tone.amplitude.set(1.0)
        tone.frequency.set(0.0)
        noise.amplitude.set(1.0)
        noise.frequency.set(0.0)
    }

    private val amplitudeStep = 1.0 / 15

    fun enable(toneChannel: Boolean, noiseChannel: Boolean) {
        tone.isEnabled = toneChannel
        noise.isEnabled = noiseChannel
    }


    fun setAmplitude(amplitude: Int) {
        noise.amplitude.set(amplitudeStep * amplitude)
        tone.amplitude.set(amplitudeStep * amplitude)
    }

    fun setTonePeriod(period: Int): Double {
        val frequency = periodToFrequency(period)
        tone.frequency.set(frequency)
        return frequency
    }

    fun setNoisePeriod(period: Int): Double {
        val frequency = periodToFrequency(period)
        noise.frequency.set(frequency)
        return frequency
    }

    private fun periodToFrequency(period: Int): Double = if (period == 0) {
        0.0
    } else {
        3546900.0 / (16 * period)
    }
}
