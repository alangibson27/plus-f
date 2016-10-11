package com.socialthingy.plusf.spectrum.display;

import java.awt.Color;

public class SpectrumColour {
    private static final int[] SPECTRUM_COLOUR_MASKS = new int[] {
            0xff000000,
            0xff0000ff,
            0xffff0000,
            0xffff00ff,
            0xff00ff00,
            0xff00ffff,
            0xffffff00,
            0xffffffff
    };

    private static final int DULL_COLOUR_MASK = 0xffcccccc;
    private static final int BRIGHT_COLOUR_MASK = 0xffffffff;

    private boolean flash;
    private int ink;
    private int paper;

    public static int dullColour(final int index) {
        return SPECTRUM_COLOUR_MASKS[index] & DULL_COLOUR_MASK;
    }

    public SpectrumColour(final int flash, final int bright, final int paper, final int ink) {
        this.flash = flash == 1;
        this.ink = toColour(bright == 1, ink);
        this.paper = toColour(bright == 1, paper);
    }

    public boolean isFlash() {
        return flash;
    }

    public int getInk() {
        return ink;
    }

    public int getPaper() {
        return paper;
    }

    private int toColour(final boolean bright, final int value) {
        final int brightnessMultiplier = bright ? BRIGHT_COLOUR_MASK : DULL_COLOUR_MASK;
        final int colour = SPECTRUM_COLOUR_MASKS[value] & brightnessMultiplier;
        return new Color((colour & 0xff0000) >> 16, (colour & 0x00ff00) >> 8, (colour & 0x0000ff)).getRGB();
    }
}
