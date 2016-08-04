package com.socialthingy.plusf.spectrum.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class Keyboard {
    private static final int[] KEY_POSITIONS = new int[] {
            binary("11110"),
            binary("11101"),
            binary("11011"),
            binary("10111"),
            binary("01111")
    };
    private static final int KEYS_DOWN_EMPTY = binary("10111111");
    private static final int NO_KEYS_DOWN = binary("11111");

    private final Set<Character> keysDown = new HashSet<>();
    private final Map<Character, Integer>[] halfRows = new Map[8];

    public Keyboard() {
        halfRows[0] = buildHalfRow("^zxcv");
        halfRows[1] = buildHalfRow("asdfg");
        halfRows[2] = buildHalfRow("qwert");
        halfRows[3] = buildHalfRow("12345");
        halfRows[4] = buildHalfRow("09876");
        halfRows[5] = buildHalfRow("poiuy");
        halfRows[6] = buildHalfRow("_lkjh");
        halfRows[7] = buildHalfRow(" $mnb");
    }

    public int readKeyboard(final int halfRowIdentifier) {
        if (keysDown.isEmpty()) {
            return KEYS_DOWN_EMPTY;
        } else {
            int bits = NO_KEYS_DOWN;
            for (int i = 0; i < 8; i++) {
                if ((halfRowIdentifier & (1 << i)) == 0) {
                    for (Character key : keysDown) {
                        if (halfRows[i].containsKey(key)) {
                            bits &= halfRows[i].get(key);
                        }
                    }
                }
            }

            return binary("10100000") | bits;
        }
    }

    public void keyDown(final char key) {
        keysDown.add(Character.toLowerCase(key));
    }

    public void keyUp(final char key) {
        keysDown.remove(Character.toLowerCase(key));
    }

    public void reset() {
        keysDown.clear();
    }

    private Map<Character, Integer> buildHalfRow(final String rowKeys) {
        final Map<Character, Integer> keyBits = new HashMap<>();
        for (int i = 0; i < KEY_POSITIONS.length; i++) {
            keyBits.put(rowKeys.charAt(i), KEY_POSITIONS[i]);
        }
        return keyBits;
    }
}
