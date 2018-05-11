package com.socialthingy.plusf.spectrum.display;

import com.socialthingy.plusf.util.UnsafeUtil;
import sun.misc.Unsafe;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class UnsafePixelMapper extends PixelMapper {
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    @Override
    public int[] getPixels(final int[] memory, final boolean flashActive) {
        long targetIdx = BASE + (258 * SCALE);
        for (int y = 0; y < 192; y++) {
            long colourIdx = BASE + (colourLineAddress(y) * SCALE);
            long pixelIdxBase = BASE + (lineAddress(y) * SCALE);
            targetIdx += SCALE;
            for (int x = 0; x < 32; x++) {
                final int colourVal = unsafe.getInt(memory, colourIdx);
                final SpectrumColour colour = colours[colourVal];
                colourIdx += SCALE;

                final int memoryVal = unsafe.getInt(memory, pixelIdxBase);
                pixelIdxBase += SCALE;

                for (int bit = 128; bit > 0; bit >>= 1) {
                    if ((memoryVal & bit) > 0) {
                        setPixel(
                            targetIdx,
                            flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk()
                        );
                    } else {
                        setPixel(
                            targetIdx,
                            flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper()
                        );
                    }
                    targetIdx += SCALE;
                }
            }
            targetIdx += SCALE;
        }

        return displayBytes;
    }

    private void setPixel(final long idx, final int color) {
        unsafe.putInt(displayBytes, idx, color);
    }
}

