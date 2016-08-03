package com.socialthingy.plusf.spectrum.display;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import java.awt.Color;

public class JavaFXDisplay extends DisplaySupport {
    public static final int BORDER = 16;
    public static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    public static final int DISPLAY_HEIGHT = SCREEN_HEIGHT + (BORDER * 2);

    private final WritableImage screen = new WritableImage(256, 192);
    private final PixelWriter screenWriter = screen.getPixelWriter();

    private final WritableImage border = new WritableImage(1, DISPLAY_HEIGHT);
    private final PixelWriter borderWriter = border.getPixelWriter();

    private final int[] pixels = new int[256 * 192];

    public Node getDisplay() {
        final ImageView borderImage = new ImageView(border);
        borderImage.setFitWidth(DISPLAY_WIDTH * 2);
        borderImage.setFitHeight(DISPLAY_HEIGHT * 2);

        final ImageView screenImage = new ImageView(screen);
        screenImage.setFitHeight(SCREEN_HEIGHT * 2);
        screenImage.setFitWidth(SCREEN_WIDTH * 2);

        return new StackPane(borderImage, screenImage);
    }

    @Override
    public void refresh(final int[] borderLines, final int[] memory, final boolean flashActive) {
        super.draw(memory, flashActive, this::setPixel);
        screenWriter.setPixels(0, 0, 256, 192, PixelFormat.getIntArgbInstance(), pixels, 0, 256);
        borderWriter.setPixels(0, 0, 1, DISPLAY_HEIGHT, PixelFormat.getIntArgbInstance(), borderLines, 0, 1);
    }

    private void setPixel(final int x, final int y, final Color color) {
        pixels[x + (y * 256)] = color.getRGB();
    }
}
