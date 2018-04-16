package com.socialthingy.plusf.spectrum.network;

import java.util.List;

public class EmulatorState {
    private int[] memory;
    private int memoryBase;
    private int memoryLength;
    private List<Long> borderChanges;
    private boolean flashActive;

    public EmulatorState(final int[] memory, final int memoryBase, final int memoryLength, final List<Long> borderChanges, final boolean flashActive) {
        this.memory = memory;
        this.memoryBase = memoryBase;
        this.memoryLength = memoryLength;
        this.borderChanges = borderChanges;
        this.flashActive = flashActive;
    }

    public int[] getMemory() {
        return memory;
    }

    public int getMemoryBase() {
        return memoryBase;
    }

    public int getMemoryLength() {
        return memoryLength;
    }

    public List<Long> getBorderChanges() {
        return borderChanges;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
