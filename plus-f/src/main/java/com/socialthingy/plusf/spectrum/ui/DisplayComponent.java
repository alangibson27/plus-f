package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.ULA;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static com.socialthingy.plusf.spectrum.display.UnsafePixelMapper.*;

abstract class DisplayComponent extends JComponent {
    protected static final int BORDER = 16;
    protected static final int SCALE = 2;
    protected static final int DISPLAY_WIDTH = (BORDER * SCALE) + (SCREEN_WIDTH * SCALE) + (BORDER * SCALE);
    protected static final int DISPLAY_HEIGHT = (BORDER * SCALE) + (SCREEN_HEIGHT * SCALE) + (BORDER * SCALE);
    protected static final Dimension DISPLAY_DIMENSIONS = new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT);
    protected static final int TSTATES_PER_LINE = 224;
    protected static final int SCALED_WIDTH = SCREEN_WIDTH * SCALE;
    protected static final int SCALED_HEIGHT = SCREEN_HEIGHT * SCALE;
    private static final int TOP_BORDER_HEIGHT = 64;
    private static final int BOTTOM_BORDER_HEIGHT = 56;

    private static int xlimit(final int x) {
        if (x < 0) {
            return 0;
        } else if (x > 255) {
            return 255;
        } else {
            return x;
        }
    }

    private static int ylimit(final int y) {
        if (y < 0) {
            return 0;
        } else if (y > 191) {
            return 191;
        } else {
            return y;
        }
    }

    static int sourcePixelAt(final int x, final int y) {
        return xlimit(x) + (ylimit(y) * SCREEN_WIDTH);
    }

    static int targetPixelAt(final int mainx, final int mainy, final int subx, final int suby) {
        return ((mainx * SwingDoubleSizeDisplay.SCALE) + subx) + (((mainy * SwingDoubleSizeDisplay.SCALE) + suby) * (SCREEN_WIDTH * SwingDoubleSizeDisplay.SCALE));
    }

    protected final int[] targetPixels;
    protected final int[] borderPixels = new int[TOP_BORDER_HEIGHT + SCREEN_HEIGHT + BOTTOM_BORDER_HEIGHT];
    protected final BufferedImage borderImage;
    protected final BufferedImage image;
    protected final int[] imageDataBuffer;
    protected final int[] borderImageDataBuffer;
    private final PixelMapper pixelMapper;

    DisplayComponent(final PixelMapper pixelMapper) {
        this.pixelMapper = pixelMapper;
        this.targetPixels = new int[SCALED_WIDTH * SCALED_HEIGHT];
        this.image = new BufferedImage(SCALED_WIDTH, SCALED_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.imageDataBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.borderImage = new BufferedImage(1, borderPixels.length, BufferedImage.TYPE_INT_ARGB);
        this.borderImageDataBuffer = ((DataBufferInt) borderImage.getRaster().getDataBuffer()).getData();

        setPreferredSize(DISPLAY_DIMENSIONS);
        setMinimumSize(DISPLAY_DIMENSIONS);
    }

    protected void renderMemory(int[] memory, boolean flashActive) {
        scale(pixelMapper.getPixels(memory, flashActive));
    }

    public abstract void updateScreen(int[] memory, ULA ula);
    public abstract void updateBorder(ULA ula, boolean force);
    protected abstract void scale(int[] sourcePixels);
}
