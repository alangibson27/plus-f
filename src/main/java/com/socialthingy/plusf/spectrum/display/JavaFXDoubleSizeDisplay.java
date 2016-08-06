package com.socialthingy.plusf.spectrum.display;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import java.awt.Color;

public class JavaFXDoubleSizeDisplay extends Display {
    public static final int BORDER = 16;
    private static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    private static final int DISPLAY_HEIGHT = SCREEN_HEIGHT + (BORDER * 2);
    private static final int SCALE = 2;

    private final WritableImage border = new WritableImage(1, DISPLAY_HEIGHT);
    private final PixelWriter borderWriter = border.getPixelWriter();
    private final PixelWriter screenWriter;

    private final Node display;
    private final int scaledWidth;
    private final int scaledHeight;
    private final int[] sourcePixels = new int[SCREEN_WIDTH * SCREEN_WIDTH];
    private final int[] targetPixels;

    public JavaFXDoubleSizeDisplay() {
        super(BORDER, BORDER);

        this.scaledWidth = SCREEN_WIDTH * SCALE;
        this.scaledHeight = SCREEN_HEIGHT * SCALE;

        final WritableImage screen = new WritableImage(scaledWidth, scaledHeight);
        this.screenWriter = screen.getPixelWriter();
        this.targetPixels = new int[scaledWidth * scaledHeight];

        final ImageView borderImage = new ImageView(border);
        borderImage.setFitWidth(DISPLAY_WIDTH * SCALE);
        borderImage.setFitHeight(DISPLAY_HEIGHT * SCALE);

        final ImageView screenImage = new ImageView(screen);
        screenImage.setFitHeight(scaledHeight);
        screenImage.setFitWidth(scaledWidth);

        this.display = new StackPane(borderImage, screenImage);
    }

    public Node getDisplay() {
        return display;
    }

    public void refresh(final int[] memory, final boolean flashActive) {
        super.draw(memory, flashActive, this::setPixel);
        scale();
        screenWriter.setPixels(0, 0, scaledWidth, scaledHeight, PixelFormat.getIntArgbInstance(), targetPixels, 0, scaledWidth);
        borderWriter.setPixels(0, 0, 1, DISPLAY_HEIGHT, PixelFormat.getIntArgbInstance(), borderLines, 0, 1);
    }

    private void setPixel(final int x, final int y, final Color color) {
        sourcePixels[sourcePixelAt(x, y)] = color.getRGB();
    }

    private void scale() {
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

                targetPixels[targetPixelAt(x * 2, y * 2, 0, 0)] = e0;
                targetPixels[targetPixelAt(x * 2, y * 2, 1, 0)] = e1;
                targetPixels[targetPixelAt(x * 2, y * 2, 0, 1)] = e2;
                targetPixels[targetPixelAt(x * 2, y * 2, 1, 1)] = e3;
            }
        }
    }

    private final int sourcePixelAt(final int x, final int y) {
        return (x & 0xff) + ((y & 0xff) * SCREEN_WIDTH);
    }

    private final int targetPixelAt(final int mainx, final int mainy, final int subx, final int suby) {
        return (mainx + subx) + ((mainy + suby) * scaledWidth);
    }
}
