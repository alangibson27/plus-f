package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.Memory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import static com.socialthingy.plusf.spectrum.display.UnsafePixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class SafeSwingDoubleSizeDisplay extends DisplayComponent {
    public SafeSwingDoubleSizeDisplay(final PixelMapper pixelMapper) {
        super(pixelMapper);
    }

    public void updateScreen(final int[] memory, final ULA ula) {
        if (Memory.screenChanged() || ula.flashStatusChanged()) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), ula.flashActive());
        }
    }

    public void updateBorder(final ULA ula, final boolean force) {
        if (force || ula.borderNeedsRedrawing()) {
            final Iterator<Long> it = new ArrayList<>(ula.getBorderChanges()).iterator();
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
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
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
        for (int x = 1; x < SCREEN_WIDTH + 1; x++) {
            for (int y = 1; y < SCREEN_HEIGHT + 1; y++) {
                final int a = sourcePixels[sourcePixelAt(x, y - 1)];
                final int c = sourcePixels[sourcePixelAt(x - 1, y)];
                final int p = sourcePixels[sourcePixelAt(x, y)];
                final int b = sourcePixels[sourcePixelAt(x + 1, y)];
                final int d = sourcePixels[sourcePixelAt(x, y + 1)];

                final int e0 = (c == a && c != d && a != b) ? a : p;
                final int e1 = (a == b && a != c && b != d) ? b : p;
                final int e2 = (d == c && d != b && c != a) ? c : p;
                final int e3 = (b == d && b != a && d != c) ? d : p;

                targetPixels[targetPixelAt(x - 1, y - 1, 0, 0)] = e0;
                targetPixels[targetPixelAt(x - 1, y - 1, 1, 0)] = e1;
                targetPixels[targetPixelAt(x - 1, y - 1, 0, 1)] = e2;
                targetPixels[targetPixelAt(x - 1, y - 1, 1, 1)] = e3;
            }
        }
    }
}
