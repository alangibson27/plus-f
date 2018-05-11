package com.socialthingy.plusf.spectrum.display;

public abstract class PixelMapper {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;
    protected final SpectrumColour[] colours = new SpectrumColour[0x100];
    protected final int[] displayBytes;

    protected PixelMapper() {
        this.displayBytes = new int[(SCREEN_WIDTH + 2) * (SCREEN_HEIGHT + 2)];

        for (int flash = 0; flash <= 1; flash++) {
            for (int bright = 0; bright <= 1; bright++) {
                for (int paper = 0; paper < 8; paper++) {
                    for (int ink = 0; ink < 8; ink ++) {
                        final int attr = (flash << 7) | (bright << 6) | (paper << 3) | ink;
                        colours[attr] = new SpectrumColour(flash, bright, paper, ink);
                    }
                }
            }
        }
    }

    public abstract int[] getPixels(int[] memory, boolean flashActive);

    protected int pixelAddress(final int x, final int y) {
        return lineAddress(y) + x;
    }

    protected int colourLineAddress(final int y) {
        return 0x5800 + (0x20 * (y >> 3));
    }

    private int lineAddress(final int y) {
        final int hi = y & 0b00111000;
        final int lo = y & 0b00000111;
        return 0x4000 + ((y >> 6) * 0x800) + (((hi >> 3) | (lo << 3)) * 32);
    }
}
