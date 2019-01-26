package com.socialthingy.plusf.snapshot;

import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_B;

public class KeyTranslator {
    private static final int[][] keyRows = new int[8][];
    private static final Map<Integer, byte[]> keyCodes = new HashMap<>();
    private static final byte[] NO_CODE = new byte[] {-1, -1};

    static {
        keyRows[0] = new int[] {VK_SHIFT, VK_Z, VK_X, VK_C, VK_V};
        keyRows[1] = new int[] {VK_A, VK_S, VK_D, VK_F, VK_G};
        keyRows[2] = new int[] {VK_Q, VK_W, VK_E, VK_R, VK_T};
        keyRows[3] = new int[] {VK_1, VK_2, VK_3, VK_4, VK_5};
        keyRows[4] = new int[] {VK_0, VK_9, VK_8, VK_7, VK_6};
        keyRows[5] = new int[] {VK_P, VK_O, VK_I, VK_U, VK_Y};
        keyRows[6] = new int[] {VK_ENTER, VK_L, VK_K, VK_J, VK_H};
        keyRows[7] = new int[] {VK_SPACE, VK_CONTROL, VK_M, VK_N, VK_B};

        for (int i = 0; i < keyRows.length; i++) {
            for (int j = 0; j < keyRows[i].length; j++) {
                keyCodes.put(keyRows[i][j], new byte[] {(byte) i, (byte) (1 << j)});
            }
        }
    }

    public static int coordinateToKeyCode(final int row, final int columnMask) {
        final int column = Integer.numberOfTrailingZeros(columnMask);
        if (row > 7 || column > 4) {
            return -1;
        }
        return keyRows[row][column];
    }

    public static byte[] keyCodeToCoordinate(final int keyCode) {
        return keyCodes.getOrDefault(keyCode, NO_CODE);
    }

    public static byte[] keyCodeToDisplayable(final int keyCode) {
        final byte modifiedKeyCode;
        switch (keyCode) {
            case VK_SHIFT:
                modifiedKeyCode = VK_OPEN_BRACKET;
                break;

            case VK_CONTROL:
                modifiedKeyCode = VK_CLOSE_BRACKET;
                break;

            case VK_ENTER:
                modifiedKeyCode = VK_SLASH;
                break;

            case VK_SPACE:
                modifiedKeyCode = VK_BACK_SLASH;
                break;

            default:
                modifiedKeyCode = (byte) keyCode;
        }
        return new byte[] {modifiedKeyCode, 0};
    }
}
