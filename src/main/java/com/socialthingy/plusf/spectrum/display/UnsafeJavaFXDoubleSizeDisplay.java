package com.socialthingy.plusf.spectrum.display;

import com.socialthingy.plusf.z80.Memory;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import java.awt.*;

public class UnsafeJavaFXDoubleSizeDisplay extends UnsafeDisplay {
    public static final int BORDER = 16;
    private static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    private static final int DISPLAY_HEIGHT = SCREEN_HEIGHT + (BORDER * 2);
    private static final int SCALE = 2;

    private static int sourcePixelAt(final int x, final int y) {
        return (x & 0xff) + ((y & 0xff) * SCREEN_WIDTH);
    }

    private static int targetPixelAt(final int mainx, final int mainy, final int subx, final int suby) {
        return ((mainx * SCALE) + subx) + (((mainy * SCALE) + suby) * (SCREEN_WIDTH * SCALE));
    }

    private final WritableImage border = new WritableImage(1, DISPLAY_HEIGHT);
    private final PixelWriter borderWriter = border.getPixelWriter();
    private final PixelWriter screenWriter;

    private final Node display;
    private final int scaledWidth;
    private final int scaledHeight;
    private final int[] sourcePixels = new int[SCREEN_WIDTH * SCREEN_WIDTH];
    private final int[] targetPixels;

    public UnsafeJavaFXDoubleSizeDisplay() {
        super(BORDER, BORDER);

        this.scaledWidth = SCREEN_WIDTH * SCALE;
        this.scaledHeight = SCREEN_HEIGHT * SCALE;

        final Canvas screen = new Canvas(scaledWidth, scaledHeight);
        this.screenWriter = screen.getGraphicsContext2D().getPixelWriter();
        this.targetPixels = new int[scaledWidth * scaledHeight];

        final ImageView borderImage = new ImageView(border);
        borderImage.setFitWidth(DISPLAY_WIDTH * SCALE);
        borderImage.setFitHeight(DISPLAY_HEIGHT * SCALE);

        this.display = new StackPane(borderImage, screen);
    }

    public Node getDisplay() {
        return display;
    }

    public boolean render(final int[] memory, final boolean flashActive, final boolean flashChanged) {
        if (Memory.screenChanged() || flashChanged) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), flashActive);
            return true;
        } else {
            return false;
        }
    }

    public void renderMemory(final int[] memory, final boolean flashActive) {
        super.draw(memory, flashActive, this::setPixel);
        scale();
    }

    public void refreshScreen() {
        screenWriter.setPixels(0, 0, scaledWidth, scaledHeight, PixelFormat.getIntArgbInstance(), targetPixels, 0, scaledWidth);
    }

    public void refreshBorder() {
        borderWriter.setPixels(0, 0, 1, DISPLAY_HEIGHT, PixelFormat.getIntArgbInstance(), borderLines, 0, 1);
    }

    private void setPixel(final int x, final int y, final Color color) {
        unsafe.putInt(sourcePixels, 16L + (sourcePixelAt(x, y) * 4), color.getRGB());
    }

    protected void scale() {
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
