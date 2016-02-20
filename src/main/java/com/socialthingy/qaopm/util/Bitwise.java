package com.socialthingy.qaopm.util;

public class Bitwise {
    public static int[] add(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return new int[] {result & 0xff, resultLow > 0x0f ? 1 : 0, result > 0xff ? 1 : 0};
    }

    public static int[] sub(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;
        return new int[] {result & 0xff, resultLow < 0x00 ? 1 : 0, result < 0x00 ? 1 : 0};
    }
}
