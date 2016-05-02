package com.socialthingy.qaopm.spectrum.display;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.Color;

public class JavaFXDisplay extends DisplaySupport<WritableImage> {
    private final WritableImage screen = new WritableImage(256, 192);
    private final PixelWriter pw = screen.getPixelWriter();
    private final int[] pixels = new int[256 * 192];

    public WritableImage getScreen() {
        return screen;
    }

    @Override
    public WritableImage refresh(final int[] memory, final boolean flashActive) {
        super.draw(memory, flashActive, this::setPixel);
        pw.setPixels(0, 0, 256, 192, PixelFormat.getIntArgbInstance(), pixels, 0, 256);
        return screen;
    }

    private void setPixel(final int x, final int y, final Color color) {
        pixels[x + (y * 256)] = color.getRGB();
    }
}
