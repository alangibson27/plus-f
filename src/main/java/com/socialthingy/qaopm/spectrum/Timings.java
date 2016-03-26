package com.socialthingy.qaopm.spectrum;

public class Timings {
    private final int interruptFrequencyHz;
    private final int screenRefreshFrequencyHz;
    private final int processorFrequencyHz;

    public Timings(final int interruptFrequencyHz, final int screenRefreshFrequencyHz, final int processorFrequencyHz) {
        this.interruptFrequencyHz = interruptFrequencyHz;
        this.screenRefreshFrequencyHz = screenRefreshFrequencyHz;
        this.processorFrequencyHz = processorFrequencyHz;
    }

    public int getTstatesPerRefresh() {
        return 69888;
    }
}
