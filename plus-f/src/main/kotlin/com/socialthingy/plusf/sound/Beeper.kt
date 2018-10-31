package com.socialthingy.plusf.sound

import com.jsyn.data.FloatSample
import com.jsyn.unitgen.*
import com.socialthingy.plusf.spectrum.Model

open class Beeper(private val sampler: VariableRateMonoReader) {
    var updatePeriod: Double = 3500000.0 / sampler.rate.get()

    private val beeperStates = FloatArray(900)
    private var beeperIdx = 0
    private var allStatesHigh = true
    private var isEnabled = false

    fun setModel(model: Model) {
        updatePeriod = model.clockFrequencyHz / sampler.rate.get()
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled

        if (!enabled) {
            beeperIdx = 0
        }
    }

    fun update(state: Boolean) {
        if (isEnabled) {
            beeperStates[beeperIdx] = if (state) 1.0F else 0.0F
            beeperIdx++
            allStatesHigh = allStatesHigh && state
        }
    }

    fun play() {
        if (beeperIdx == 0) {
            sampler.dataQueue.clear()
        } else {
            if (!isEnabled || allStatesHigh) {
                sampler.dataQueue.clear()
            } else {
                val sample = FloatSample()
                sample.allocate(beeperIdx, 1)
                sample.write(0, beeperStates, 0, beeperIdx)
                sampler.dataQueue.queue(sample)
            }
            beeperIdx = 0
            allStatesHigh = true
        }
    }
}
