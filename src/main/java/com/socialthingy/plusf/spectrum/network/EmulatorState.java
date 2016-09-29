package com.socialthingy.plusf.spectrum.network;

import static com.socialthingy.plusf.spectrum.display.Display.SCREEN_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay.BORDER;

public class EmulatorState {
    public static final int BORDER_LINE_COUNT = SCREEN_HEIGHT + (2 * BORDER);

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
