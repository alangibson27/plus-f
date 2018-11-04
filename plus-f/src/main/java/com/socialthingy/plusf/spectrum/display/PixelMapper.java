package com.socialthingy.plusf.spectrum.display;

public class PixelMapper {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;
    protected final SpectrumColour[] colours = new SpectrumColour[0x100];
    protected final int[] displayBytes;

    public PixelMapper() {
        this.displayBytes = new int[(SCREEN_WIDTH + 2) * (SCREEN_HEIGHT + 2)];

        for (int flash = 0; flash <= 1; flash++) {
            for (int bright = 0; bright <= 1; bright++) {
                for (int paper = 0; paper < 8; paper++) {
                    for (int ink = 0; ink < 8; ink ++) {
                        final int attr = (flash << 7) | (bright << 6) | (paper << 3) | ink;
                        colours[attr] = new SpectrumColour(flash, bright, paper, ink);
                    }
                }
            }
        }
    }

    protected int colourLineAddress(final int y) {
        return 0x1800 + 0x20 * (y >> 3);
    }

    protected int lineAddress(final int y) {
        final int hi = y & 0b00111000;
        final int lo = y & 0b00000111;
        return (y >> 6) * 0x800 + (hi >> 3 | lo << 3) * 32;
    }

    public int[] getPixels(final int[] memory, final boolean flashActive) {
        int targetIdx = 258;
        for (int y = 0; y < 192; y++) {
            int colourIdx = colourLineAddress(y);
            int pixelIdxBase = lineAddress(y);
            targetIdx += 1;
            for (int x = 0; x < 32; x++) {
                final int colourVal = memory[colourIdx];
                final SpectrumColour colour = colours[colourVal];
                colourIdx += 1;

                final int ink = flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk();
                final int paper = flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper();

                final int memoryVal = memory[pixelIdxBase];
                pixelIdxBase += 1;

                for (int bit = 128; bit > 0; bit >>= 1) {
                    displayBytes[targetIdx] = (memoryVal & bit) > 0 ? ink : paper;
                    targetIdx += 1;
                }
            }
            targetIdx += 1;
        }

        return displayBytes;
    }

    public void renderScanline(
            final int[] displayMemory,
            final int[] pixels,
            final int scanline,
            final boolean flashActive
    ) {
        int targetIdx = (SCREEN_WIDTH + 2) * (scanline - 63) + 1;
        int pixelSource = lineAddress(scanline - 64);
        int attrSource = colourLineAddress(scanline - 64);

        for (int attr = 0; attr < 32; attr++) {
            final SpectrumColour colour = colours[displayMemory[attrSource++]];
            final int ink = flashActive && colour.isFlash() ? colour.getPaper() : colour.getInk();
            final int paper = flashActive && colour.isFlash() ? colour.getInk() : colour.getPaper();

            final int pixelGroup = displayMemory[pixelSource++];
            for (int pixel = 128; pixel > 0; pixel >>= 1) {
                pixels[targetIdx++] = (pixelGroup & pixel) > 0 ? ink : paper;
            }
        }
    }
}
