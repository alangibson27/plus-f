package com.socialthingy.plusf.spectrum.display;

import java.awt.Color;

public class SpectrumColour {
    private static final int[] SPECTRUM_COLOUR_MASKS = new int[] {
            0x000000,
            0x0000ff,
            0xff0000,
            0xff00ff,
            0x00ff00,
            0x00ffff,
            0xffff00,
            0xffffff
    };

    private static final int DULL_COLOUR_MASK = 0xcccccc;
    private static final int BRIGHT_COLOUR_MASK = 0xffffff;

    private boolean flash;
    private Color ink;
    private int inkRgb;
    private Color paper;
    private int paperRgb;

    public static int dullColour(final int index) {
        return SPECTRUM_COLOUR_MASKS[index] & DULL_COLOUR_MASK;
    }

    public SpectrumColour(final int flash, final int bright, final int paper, final int ink) {
        this.flash = flash == 1;
        this.ink = toColour(bright == 1, ink);
        this.inkRgb = this.ink.getRGB();
        this.paper = toColour(bright == 1, paper);
        this.paperRgb = this.paper.getRGB();
    }

    public boolean isFlash() {
        return flash;
    }

    public Color getInk() {
        return ink;
    }

    public Color getPaper() {
        return paper;
    }

    public int getInkRgb() {
        return inkRgb;
    }

    public int getPaperRgb() {
        return paperRgb;
    }

    private Color toColour(final boolean bright, final int value) {
        final int brightnessMultiplier = bright ? BRIGHT_COLOUR_MASK : DULL_COLOUR_MASK;
        final int colour = SPECTRUM_COLOUR_MASKS[value] & brightnessMultiplier;
        return new Color((colour & 0xff0000) >> 16, (colour & 0x00ff00) >> 8, (colour & 0x0000ff));
    }
}
