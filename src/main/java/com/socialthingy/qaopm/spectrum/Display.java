package com.socialthingy.qaopm.spectrum;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final int[] memory;
    private final SpectrumColour[] colours = new SpectrumColour[0x100];
    private final WritableImage screen = new WritableImage(256, 192);

    private int[][] pixelAddresses = new int[192][];
    private int[][] colourAddresses = new int[192][];

    public Display(final int[] memory) {
        this.memory = memory;

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

    public WritableImage getScreen() {
        return screen;
    }

    public WritableImage refresh() {
        final WritableImage image = new WritableImage(256, 192);
        final PixelWriter pw = image.getPixelWriter();

        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 32; x++) {
                final SpectrumColour colour = colours[memory[colourAddresses[y][x]]];

                for (int bit = 0; bit < 8; bit++) {
                    final int pixelAddress = pixelAddresses[y][x];
                    final int displayX = (x * 8) + (7 - bit);
                    if ((memory[pixelAddress] & (1 << bit)) > 0) {
                        pw.setColor(displayX, y, colour.getInk());
                    } else {
                        pw.setColor(displayX, y, colour.getPaper());
                    }
                }
            }
        }
        return image;
    }

    private int getLineAddress(final int y) {
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

}

class SpectrumColour {
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

    private boolean flash;
    private Color ink;
    private Color paper;

    public SpectrumColour(final int flash, final int bright, final int paper, final int ink) {
        this.flash = flash == 1;
        this.ink = toColour(bright == 1, ink);
        this.paper = toColour(bright == 1, paper);
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

    private Color toColour(final boolean bright, final int value) {
        final int brightnessMultiplier = bright ? 0xffffff : 0xaaaaaa;
        final int colour = SPECTRUM_COLOUR_MASKS[value] & brightnessMultiplier;
        return Color.rgb((colour & 0xff0000) >> 16, (colour & 0x00ff00) >> 8, (colour & 0x0000ff));
    }
}