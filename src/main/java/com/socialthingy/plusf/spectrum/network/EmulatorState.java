package com.socialthingy.plusf.spectrum.network;

import static com.socialthingy.plusf.spectrum.display.Screen.BOTTOM_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.Screen.SCREEN_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.Screen.TOP_BORDER_HEIGHT;

public class EmulatorState {
    public static final int BORDER_LINE_COUNT = TOP_BORDER_HEIGHT + SCREEN_HEIGHT + BOTTOM_BORDER_HEIGHT;

    private int[] memory;
    private int[] borderLines;
    private boolean flashActive;

    public EmulatorState(final int[] memory, final int[] borderLines, final boolean flashActive) {
        this.memory = memory;
        this.borderLines = borderLines;
        this.flashActive = flashActive;
    }

    public int[] getMemory() {
        return memory;
    }

    public int[] getBorderLines() {
        return borderLines;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
