package com.socialthingy.plusf.spectrum.display;

import com.socialthingy.plusf.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class Display implements Renderer {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;

    public static final int TOP_BORDER_HEIGHT = 64;
    public static final int BOTTOM_BORDER_HEIGHT = 56;

    private final SpectrumColour[] colours = new SpectrumColour[0x100];
    private final List<int[]> borderChanges = new ArrayList<>();
    private int initialBorderColour;
    private final int visibleDisplayStart;
    private final int visibleDisplayEnd;
    protected final int[] borderLines;

    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

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
    }

    private int pixelAddress(final int x, final int y) {
        return lineAddress(y) + x;
    }

    private int colourAddress(final int x, final int y) {
        return 0x5800 + x + (0x20 * (y >> 3));
    }

    private int lineAddress(final int y) {
        final int hi = y & 0b00111000;
        final int lo = y & 0b00000111;
        return 0x4000 + ((y >> 6) * 0x800) + (((hi >> 3) | (lo << 3)) * 32);
    }

    protected void draw(final int[] memory, final boolean flashActive) {
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 32; x++) {
                final int colourVal = unsafe.getInt(memory, 16L + (colourAddress(x, y) * 4));
                final SpectrumColour colour = colours[colourVal];

                final int pixelAddress = pixelAddress(x, y);
                final int memoryVal = unsafe.getInt(memory, 16L + (pixelAddress * 4));
                int displayX = (x * 8) + 7;
                for (int bit = 0; bit < 8; bit++) {
                    if ((memoryVal & (1 << bit)) > 0) {
                        setPixel(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk()
                        );
                    } else {
                        setPixel(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper()
                        );
                    }
                    displayX--;
                }
            }
        }
    }

    protected void setPixel(final int x, final int y, final Color color) {}

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

    @Override
    public void renderMemory(int[] memory, boolean flashActive) {
    }
}

