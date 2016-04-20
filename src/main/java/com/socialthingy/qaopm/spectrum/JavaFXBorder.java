package com.socialthingy.qaopm.spectrum;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import static com.socialthingy.qaopm.spectrum.UIBuilder.DISPLAY_HEIGHT;

class JavaFXBorder {
    private final WritableImage border = new WritableImage(1, DISPLAY_HEIGHT);
    private final PixelWriter pw = border.getPixelWriter();

    public WritableImage getBorder() {
        return border;
    }

    public WritableImage refresh(final int[] borderLines) {
        pw.setPixels(0, 0, 1, DISPLAY_HEIGHT, PixelFormat.getIntArgbInstance(), borderLines, 0, 1);
        return border;
    }
}
