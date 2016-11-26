package com.socialthingy.plusf.spectrum.network;

import java.util.List;

public class EmulatorState {
    private int[] memory;
    private List<Long> borderChanges;
    private boolean flashActive;

    public EmulatorState(final int[] memory, final List<Long> borderChanges, final boolean flashActive) {
        this.memory = memory;
        this.borderChanges = borderChanges;
        this.flashActive = flashActive;
    }

    public int[] getMemory() {
        return memory;
    }

    public List<Long> getBorderChanges() {
        return borderChanges;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
