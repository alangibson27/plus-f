package com.socialthingy.plusf.spectrum.network;

public class EmulatorState {
    private int[] memory;
    private int memoryBase;
    private int memoryLength;
    private int[] borderColours;
    private boolean flashActive;

    public EmulatorState(final int[] memory, final int memoryBase, final int memoryLength, final int[] borderColours, final boolean flashActive) {
        this.memory = memory;
        this.memoryBase = memoryBase;
        this.memoryLength = memoryLength;
        this.borderColours = borderColours;
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

    public int[] getBorderColours() {
        return borderColours;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
