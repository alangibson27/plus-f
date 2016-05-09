package com.socialthingy.plusf.util;

public class Bitwise {
    public static int[] add(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return new int[] {result & 0xff, resultLow > 0x0f ? 1 : 0, result > 0xff ? 1 : 0};
    }

    public static int[] addWord(final int v1, final int v2) {
        final int v1Low = v1 & 0x0fff;
        final int v2Low = v2 & 0x0fff;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return new int[] {result & 0xffff, resultLow > 0x0fff ? 1 : 0, result > 0xffff ? 1 : 0};
    }

    public static int[] subWord(final int v1, final int v2) {
        final int v1Low = v1 & 0x0fff;
        final int v2Low = v2 & 0x0fff;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;
        return new int[] {result & 0xffff, resultLow < 0 ? 1 : 0, result < 0 ? 1 : 0};
    }

    public static int[] sub(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;
        return new int[] {result & 0xff, resultLow < 0x00 ? 1 : 0, result < 0x00 ? 1 : 0};
    }

    public static boolean hasParity(int value) {
        boolean parity = true;
        for (int i = 0; i < 8; i++) {
            if ((value & 0b1) > 0) {
                parity = !parity;
            }
            value >>= 1;
        }
        return parity;
    }

    public static int[] nibbles(final int value) {
        final int[] nibbles = new int[2];
        nibbles[0] = (value & 0b11110000) >> 4;
        nibbles[1] = value & 0b1111;
        return nibbles;
    }
}
