package com.socialthingy.qaopm.spectrum.remote;

import java.io.Serializable;

public class SpectrumState implements Serializable {
    private int[] screen;
    private int[] borderLines;
    private boolean flashActive;

    public SpectrumState(final int[] screen, final int[] borderLines, final boolean flashActive) {
        this.screen = screen;
        this.borderLines = borderLines;
        this.flashActive = flashActive;
    }

    public int[] getScreen() {
        return screen;
    }

    public int[] getBorderLines() {
        return borderLines;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
