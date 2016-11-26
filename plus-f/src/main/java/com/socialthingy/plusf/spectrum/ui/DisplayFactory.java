package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.SafePixelMapper;
import com.socialthingy.plusf.spectrum.display.UnsafePixelMapper;

public class DisplayFactory {
    private DisplayFactory() {}

    public static DisplayComponent create() {
        if (System.getProperty("safeDisplay") == null) {
            System.out.println("Using unsafe display");
            return new SwingDoubleSizeDisplay(new UnsafePixelMapper());
        } else {
            System.out.println("Using safe display");
            return new SafeSwingDoubleSizeDisplay(new SafePixelMapper());
        }
    }
}
