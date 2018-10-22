package com.socialthingy.plusf.spectrum.display;

import static com.socialthingy.plusf.spectrum.display.PixelMapper.SCREEN_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.PixelMapper.SCREEN_WIDTH;

public class Scaler2X {
    public static final int SCALE = 2;
    private static final int DEST_SCAN_WIDTH = SCREEN_WIDTH * SCALE * SCALE;
    private static final int SRC_SCAN_WIDTH = SCREEN_WIDTH + 2;

    public static void scale(final int[] sourcePixels, final int[] destPixels) {
        for (int x = 1; x < SCREEN_WIDTH + 1; x++) {
            int e0idx = (x - 1) * 2;
            int e1idx = e0idx + 1;
            int e2idx = e0idx + (SCREEN_WIDTH * 2);
            int e3idx = e2idx + 1;

            int pIdx = x + SCREEN_WIDTH + 2;
            int aIdx = x;
            int cIdx = pIdx - 1;
            int bIdx = pIdx + 1;
            int dIdx = pIdx + SCREEN_WIDTH + 2;

            for (int y = 1; y < SCREEN_HEIGHT + 1; y++) {
                final int a = sourcePixels[aIdx];
                final int c = sourcePixels[cIdx];
                final int p = sourcePixels[pIdx];
                final int b = sourcePixels[bIdx];
                final int d = sourcePixels[dIdx];
                aIdx += SRC_SCAN_WIDTH;
                cIdx += SRC_SCAN_WIDTH;
                pIdx += SRC_SCAN_WIDTH;
                bIdx += SRC_SCAN_WIDTH;
                dIdx += SRC_SCAN_WIDTH;

                destPixels[e0idx] = (c == a && c != d && a != b) ? a : p;
                destPixels[e1idx] = (a == b && a != c && b != d) ? b : p;
                destPixels[e2idx] = (d == c && d != b && c != a) ? c : p;
                destPixels[e3idx] = (b == d && b != a && d != c) ? d : p;

                e0idx += DEST_SCAN_WIDTH;
                e1idx += DEST_SCAN_WIDTH;
                e2idx += DEST_SCAN_WIDTH;
                e3idx += DEST_SCAN_WIDTH;
            }
        }
    }
}
