package com.socialthingy.qaopm.spectrum.remote;

import java.io.Serializable;

public class SpectrumState implements Serializable {
    private byte[] screen;
    private int[] borderLines;
    private boolean flashActive;

    public SpectrumState(final byte[] screen, final int[] borderLines, final boolean flashActive) {
        this.screen = screen;
        this.borderLines = borderLines;
        this.flashActive = flashActive;
    }

    public byte[] getScreen() {
        return screen;
    }

    public int[] getBorderLines() {
        return borderLines;
    }

    public boolean isFlashActive() {
        return flashActive;
    }
}
