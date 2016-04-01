package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;

import java.io.Serializable;
import java.util.ArrayList;

public class HostData implements Serializable {
    private final ArrayList<KeyCode> allowedKeys;
    private final byte[] screen;
    private final int[] borderLines;
    private final boolean flashActive;

    public HostData(
        final ArrayList<KeyCode> allowedKeys,
        final byte[] screen,
        final int[] borderLines,
        final boolean flashActive
    ) {
        this.allowedKeys = allowedKeys;
        this.screen = screen;
        this.borderLines = borderLines;
        this.flashActive = flashActive;
    }

    public ArrayList<KeyCode> getAllowedKeys() {
        return allowedKeys;
    }

    public byte[] getScreen() {
        return screen;
    }

    public int[] getBorderLines() {
        return borderLines;
    }

    public boolean getFlashActive() {
        return flashActive;
    }
}
