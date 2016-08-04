package com.socialthingy.plusf.spectrum.display;

import java.util.ArrayList;
import java.util.List;

import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class Display {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;

    public static final int TOP_BORDER_HEIGHT = 64;
    public static final int BOTTOM_BORDER_HEIGHT = 56;

    private final SpectrumColour[] colours = new SpectrumColour[0x100];
    private final List<int[]> borderChanges = new ArrayList<>();
    private int[][] pixelAddresses = new int[192][];
    private int[][] colourAddresses = new int[192][];
    private int initialBorderColour;
    private final int visibleDisplayStart;
    private final int visibleDisplayEnd;
    protected final int[] borderLines;

    public Display(final int topVisibleBorder, final int bottomVisibleBorder) {
        final int displayHeight = topVisibleBorder + SCREEN_HEIGHT + bottomVisibleBorder;
        this.borderLines = new int[displayHeight];
        this.visibleDisplayStart = TOP_BORDER_HEIGHT - topVisibleBorder;
        this.visibleDisplayEnd = TOP_BORDER_HEIGHT + SCREEN_HEIGHT + bottomVisibleBorder;

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

    public void setBorder(final int spectrumColour) {
        this.initialBorderColour = 0xff000000 | dullColour(spectrumColour);
    }

    public void changeBorder(int currentCycleTstates, int value) {
        borderChanges.add(new int[] {currentCycleTstates, value & 0b111});
    }

    public void redrawBorder() {
        if (!borderChanges.isEmpty()) {
            final int[] firstChange = borderChanges.get(0);
            for (int j = 0; j < firstChange[0] / 224; j++) {
                setBorderLine(j, initialBorderColour);
            }

            for (int i = 0; i < borderChanges.size() - 1; i++) {
                final int[] change1 = borderChanges.get(i);
                final int[] change2 = borderChanges.get(i + 1);

                final int blockStart = change1[0] / 224;
                final int blockEnd = change2[0] / 224;

                final int colour = 0xff000000 | dullColour(change1[1]);
                for (int j = blockStart; j < blockEnd; j++) {
                    setBorderLine(j, colour);
                }
            }

            final int[] finalChange = borderChanges.get(borderChanges.size() - 1);
            final int colour = 0xff000000 | dullColour(finalChange[1]);
            for (int j = finalChange[0] / 224; j < borderLines.length; j++) {
                setBorderLine(j, colour);
            }
            initialBorderColour = colour;
            borderChanges.clear();
        } else {
            for (int i = 0; i < borderLines.length; i++) {
                borderLines[i] = initialBorderColour;
            }
        }
    }

    private void setBorderLine(final int line, final int colourId) {
        if (line >= visibleDisplayStart && line < visibleDisplayEnd) {
            borderLines[line - visibleDisplayStart] = colourId;
        }
    }

    public int[] getBorderLines() {
        return borderLines;
    }
}
