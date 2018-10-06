package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;

public class DisplayFactory {
    private DisplayFactory() {}

    public static DisplayComponent create() {
        return new SwingDoubleSizeDisplay(new PixelMapper());
    }
}
