package com.socialthingy.plusf.spectrum.display;

import com.socialthingy.plusf.util.UnsafeUtil;
import sun.misc.Unsafe;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class UnsafePixelMapper extends PixelMapper {
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    @Override
    public int[] getPixels(final int[] memory, final boolean flashActive) {
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 32; x++) {
                final int colourVal = unsafe.getInt(memory, BASE + (colourAddress(x, y) * SCALE));
                final SpectrumColour colour = colours[colourVal];

                final int pixelAddress = pixelAddress(x, y);
                final int memoryVal = unsafe.getInt(memory, BASE + (pixelAddress * SCALE));
                int displayX = (x * 8) + 7;
                for (int bit = 1; bit < 256; bit <<= 1) {
                    if ((memoryVal & bit) > 0) {
                        setPixel(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk()
                        );
                    } else {
                        setPixel(
                            displayX,
                            y,
                            flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper()
                        );
                    }
                    displayX--;
                }
            }
        }

        return displayBytes;
    }

    private void setPixel(final int x, final int y, final int color) {
        unsafe.putInt(displayBytes, 16L + ((x + (y * SCREEN_WIDTH)) * 4), color);
    }
}

