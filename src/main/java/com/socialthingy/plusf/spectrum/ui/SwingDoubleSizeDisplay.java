package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Memory;
import sun.misc.Unsafe;

import java.awt.*;
import java.util.*;

import static com.socialthingy.plusf.spectrum.display.PixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class SwingDoubleSizeDisplay extends DisplayComponent {
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public SwingDoubleSizeDisplay(final PixelMapper pixelMapper) {
        super(pixelMapper);
    }

    @Override
    public void updateScreen(final int[] memory, final ULA ula) {
        if (Memory.screenChanged() || ula.flashStatusChanged()) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), ula.flashActive());
            System.arraycopy(targetPixels, 0, imageDataBuffer, 0, imageDataBuffer.length);
        }
    }

    @Override
    public void updateBorder(final ULA ula, final boolean force) {
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
            System.arraycopy(borderPixels, 0, borderImageDataBuffer, 0, borderImageDataBuffer.length);
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
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

    @Override
    protected void scale(final int[] sourcePixels) {
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                final int b = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y - 1) * 4));
                final int d = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x - 1, y) * 4));
                final int e = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y) * 4));
                final int f = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x + 1, y) * 4));
                final int h = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y + 1) * 4));

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

                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 0, 0) * 4), e0);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 1, 0) * 4), e1);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 0, 1) * 4), e2);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 1, 1) * 4), e3);
            }
        }
    }
}
