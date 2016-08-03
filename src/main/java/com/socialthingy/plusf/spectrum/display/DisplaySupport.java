package com.socialthingy.plusf.spectrum.display;

public abstract class DisplaySupport {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;

    protected final SpectrumColour[] colours = new SpectrumColour[0x100];
    protected int[][] pixelAddresses = new int[192][];
    protected int[][] colourAddresses = new int[192][];

    protected DisplaySupport() {
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

        for (int y = 0; y < 192; y++) {
            final int lineAddress = getLineAddress(y);
            final int colourAddress = 0X5800 + (0X20 * (y / 8));
            pixelAddresses[y] = new int[32];
            colourAddresses[y] = new int[32];

            for (int x = 0; x < 32; x++) {
                pixelAddresses[y][x] = lineAddress + x;
                colourAddresses[y][x] = colourAddress + x;
            }
        }
    }

    protected int getLineAddress(final int y) {
        final int hi = y & 0b00111000;
        final int lo = y & 0b00000111;
        final int line = (hi >> 3) | (lo << 3);

        final int addressBase;
        if (y < 0x40) {
            addressBase = 0x4000;
        } else if (y < 0x80) {
            addressBase = 0x4800;
        } else {
            addressBase = 0x5000;
        }

        return addressBase + (line * 32);
    }

    public abstract void refresh(final int[] borderLines, final int[] memory, final boolean flashActive);

    protected void draw(final int[] memory, final boolean flashActive, final DisplayPixelUpdate updateFunction) {
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 32; x++) {
                final SpectrumColour colour = colours[memory[colourAddresses[y][x]]];

                for (int bit = 0; bit < 8; bit++) {
                    final int pixelAddress = pixelAddresses[y][x];
                    final int displayX = (x * 8) + (7 - bit);
                    if ((memory[pixelAddress] & (1 << bit)) > 0) {
                        updateFunction.update(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk()
                        );
                    } else {
                        updateFunction.update(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper()
                        );
                    }
                }
            }
        }
    }

}

