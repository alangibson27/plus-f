package com.socialthingy.plusf.snapshot;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_B;

public class KeyIdentifier {
    private static final int[][] keyRows = new int[8][];

    static {
        keyRows[0] = new int[] {VK_SHIFT, VK_Z, VK_X, VK_C, VK_V};
        keyRows[1] = new int[] {VK_A, VK_S, VK_D, VK_F, VK_G};
        keyRows[2] = new int[] {VK_Q, VK_W, VK_E, VK_R, VK_T};
        keyRows[3] = new int[] {VK_1, VK_2, VK_3, VK_4, VK_5};
        keyRows[4] = new int[] {VK_0, VK_9, VK_8, VK_7, VK_6};
        keyRows[5] = new int[] {VK_P, VK_O, VK_I, VK_U, VK_Y};
        keyRows[6] = new int[] {VK_ENTER, VK_L, VK_K, VK_J, VK_H};
        keyRows[7] = new int[] {VK_SPACE, VK_CONTROL, VK_M, VK_N, VK_B};
    }

    public static int identify(final int row, final int columnMask) {
        final int column = Integer.numberOfTrailingZeros(columnMask);
        if (row > 7 || column > 4) {
            return -1;
        }
        return keyRows[row][column];
    }
}
