package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.Screen;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Memory;
import sun.misc.Unsafe;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;

import static com.socialthingy.plusf.spectrum.display.Screen.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class SwingDoubleSizeDisplay extends JComponent {
    private static final int BORDER = 16;
    private static final int SCALE = 2;
    private static final int DISPLAY_WIDTH = (BORDER * SCALE) + (SCREEN_WIDTH * SCALE) + (BORDER * SCALE);
    private static final int DISPLAY_HEIGHT = (BORDER * SCALE) + (SCREEN_HEIGHT * SCALE) + (BORDER * SCALE);
    private static final Dimension DISPLAY_DIMENSIONS = new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT);
    private static final int TSTATES_PER_LINE = 224;

    private static int sourcePixelAt(final int x, final int y) {
        return (x & 0xff) + ((y & 0xff) * SCREEN_WIDTH);
    }

    private static int targetPixelAt(final int mainx, final int mainy, final int subx, final int suby) {
        return ((mainx * SCALE) + subx) + (((mainy * SCALE) + suby) * (SCREEN_WIDTH * SCALE));
    }

    private final int scaledWidth;
    private final int scaledHeight;
    private final int[] targetPixels;
    private final int[] borderPixels = new int[TOP_BORDER_HEIGHT + SCREEN_HEIGHT + BOTTOM_BORDER_HEIGHT];
    private final int[] memory;
    private final Screen screen;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();
    private final BufferedImage borderImage;
    private final BufferedImage image;
    private final ULA ula;
    private final int[] imageDataBuffer;
    private final int[] borderImageDataBuffer;

    public SwingDoubleSizeDisplay(final Screen screen, final int[] memory, final ULA ula) {
        this.memory = memory;
        this.ula = ula;
        this.screen = screen;
        this.scaledWidth = SCREEN_WIDTH * SCALE;
        this.scaledHeight = SCREEN_HEIGHT * SCALE;

        this.targetPixels = new int[scaledWidth * scaledHeight * SCALE];
        this.image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        this.imageDataBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.borderImage = new BufferedImage(1, borderPixels.length, BufferedImage.TYPE_INT_ARGB);
        this.borderImageDataBuffer = ((DataBufferInt) borderImage.getRaster().getDataBuffer()).getData();
    }

    public Screen getScreen() {
        return screen;
    }

    public void updateScreen() {
        if (Memory.screenChanged() || ula.flashStatusChanged()) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), ula.flashActive());
            System.arraycopy(targetPixels, 0, imageDataBuffer, 0, imageDataBuffer.length);
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

    private void renderMemory(final int[] memory, final boolean flashActive) {
        scale(screen.draw(memory, flashActive));
    }

    @Override
    public Dimension getPreferredSize() {
        return DISPLAY_DIMENSIONS;
    }

    @Override
    public Dimension getMaximumSize() {
        return DISPLAY_DIMENSIONS;
    }

    @Override
    public Dimension getMinimumSize() {
        return DISPLAY_DIMENSIONS;
    }

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
