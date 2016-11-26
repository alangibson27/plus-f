package com.socialthingy.plusf.util;

public class Bitwise {
    public static final int HALF_CARRY_BIT = 1 << 16;
    public static final int FULL_CARRY_BIT = 1 << 17;

    public static int binary(final String value) {
        return Integer.valueOf(value, 2);
    }

    public static int add(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return result & 0xff | (resultLow > 0x0f ? HALF_CARRY_BIT : 0) | (result > 0xff ? FULL_CARRY_BIT : 0);
    }

    public static int addWord(final int v1, final int v2) {
        final int v1Low = v1 & 0x0fff;
        final int v2Low = v2 & 0x0fff;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return result & 0xffff | (resultLow > 0x0fff ? HALF_CARRY_BIT : 0) | (result > 0xffff ? FULL_CARRY_BIT : 0);
    }

    public static int subWord(final int v1, final int v2) {
        final int v1Low = v1 & 0x0fff;
        final int v2Low = v2 & 0x0fff;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;
        return result & 0xffff | (resultLow < 0 ? HALF_CARRY_BIT : 0) | (result < 0 ? FULL_CARRY_BIT : 0);
    }

    public static int sub(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;

        return result & 0xff | (resultLow < 0x00 ? HALF_CARRY_BIT : 0) | (result < 0x00 ? FULL_CARRY_BIT : 0);
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
}
