package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.Memory;

import java.awt.*;
import java.util.Iterator;

import static com.socialthingy.plusf.spectrum.display.UnsafePixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class SafeSwingDoubleSizeDisplay extends DisplayComponent {
    public SafeSwingDoubleSizeDisplay(final PixelMapper pixelMapper, final int[] memory, final ULA ula) {
        super(pixelMapper, ula, memory);
    }

    public void updateScreen() {
        if (Memory.screenChanged() || ula.flashStatusChanged()) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), ula.flashActive());
        }
    }

    public void updateBorder(final boolean force) {
        if (force || ula.borderNeedsRedrawing()) {
            final Iterator<Long> it = ula.getBorderChanges().iterator();
            long change = it.next();
            int colour = dullColour((int) change);
            int topLine = ((int) (change >> 32)) / TSTATES_PER_LINE;
            while (it.hasNext()) {
                change = it.next();
                int bottomLine = ((int) (change >> 32)) / TSTATES_PER_LINE;
                if (bottomLine > borderPixels.length) {
                    bottomLine = borderPixels.length;
                }
                for (int i = topLine; i < bottomLine; i++) {
                    borderPixels[i] = colour;
                }
                colour = dullColour((int) change);
                topLine = bottomLine;
            }
            for (int i = topLine; i < borderPixels.length; i++) {
                borderPixels[i] = colour;
            }
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        image.setRGB(0, 0, SCALED_WIDTH, SCALED_HEIGHT, targetPixels, 0, SCALED_WIDTH);
        borderImage.setRGB(0, 0, 1, borderPixels.length, borderPixels, 0, 1);
        g.drawImage(borderImage, 0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);
        g.drawImage(
                image,
                BORDER * SCALE,
                BORDER * SCALE,
                SCREEN_WIDTH * SCALE,
                SCREEN_HEIGHT * SCALE,
                null
        );
    }

    protected void scale(final int[] sourcePixels) {
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                final int b = sourcePixels[sourcePixelAt(x, y - 1)];
                final int d = sourcePixels[sourcePixelAt(x - 1, y)];
                final int e = sourcePixels[sourcePixelAt(x, y)];
                final int f = sourcePixels[sourcePixelAt(x + 1, y)];
                final int h = sourcePixels[sourcePixelAt(x, y + 1)];

                final int e0;
                final int e1;
                final int e2;
                final int e3;
                if (b != h && d != f) {
                    e0 = d == b ? d : e;
                    e1 = b == f ? f : e;
                    e2 = d == h ? d : e;
                    e3 = h == f ? f : e;
                } else {
                    e0 = e;
                    e1 = e;
                    e2 = e;
                    e3 = e;
                }

                targetPixels[targetPixelAt(x, y, 0, 0)] = e0;
                targetPixels[targetPixelAt(x, y, 1, 0)] = e1;
                targetPixels[targetPixelAt(x, y, 0, 1)] = e2;
                targetPixels[targetPixelAt(x, y, 1, 1)] = e3;
            }
        }
    }
}
