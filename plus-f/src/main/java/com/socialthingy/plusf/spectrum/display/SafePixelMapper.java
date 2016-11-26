package com.socialthingy.plusf.spectrum.display;

public class SafePixelMapper extends PixelMapper {
    @Override
    public int[] getPixels(final int[] memory, final boolean flashActive) {
        for (int y = 0; y < 192; y++) {
            for (int x = 0; x < 32; x++) {
                final int colourVal = memory[colourAddress(x, y)];
                final SpectrumColour colour = colours[colourVal];

                final int pixelAddress = pixelAddress(x, y);
                final int memoryVal = memory[pixelAddress];
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
        displayBytes[x + (y * SCREEN_WIDTH)] = color;
    }
}

