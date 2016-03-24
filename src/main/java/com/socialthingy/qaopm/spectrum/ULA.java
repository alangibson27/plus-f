package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.z80.IO;

import java.util.*;

import static com.socialthingy.qaopm.spectrum.SpectrumColour.dullColour;

public class ULA implements IO {

    private static final int[] KEY_POSITIONS = new int[] {
        binary("11110"),
        binary("11101"),
        binary("11011"),
        binary("10111"),
        binary("01111")
    };

    private int[] borderLines = new int[256 + 96];
    private Set<Character> keysDown = new HashSet<>();
    private Map<Character, Integer>[] halfRows = new Map[8];

    public ULA() {
        halfRows[0] = buildHalfRow("^zxcv");
        halfRows[1] = buildHalfRow("asdfg");
        halfRows[2] = buildHalfRow("qwert");
        halfRows[3] = buildHalfRow("12345");
        halfRows[4] = buildHalfRow("09876");
        halfRows[5] = buildHalfRow("poiuy");
        halfRows[6] = buildHalfRow("_lkjh");
        halfRows[7] = buildHalfRow(" $mnb");

        final Random random = new Random();
        for (int i = 0; i < borderLines.length; i++) {
            borderLines[i] = 0xff000000 | random.nextInt(0xffffff);
        }
    }

    private Map<Character, Integer> buildHalfRow(final String rowKeys) {
        final Map<Character, Integer> keyBits = new HashMap<>();
        for (int i = 0; i < KEY_POSITIONS.length; i++) {
            keyBits.put(rowKeys.charAt(i), KEY_POSITIONS[i]);
        }
        return keyBits;
    }

    @Override
    public int read(int port, int accumulator) {
        if (port == 0xfe) {
            if (keysDown.isEmpty()) {
                return binary("10111111");
            } else {
                int bits = binary("11111");
                for (int i = 0; i < 8; i++) {
                    if ((accumulator & (1 << i)) == 0) {
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
        return 0;
    }

    @Override
    public void write(int port, int accumulator, int value) {
        if (port == 0xfe) {
            final int borderColour = 0xff000000 | dullColour(value & 0b111);
            for (int i = 0; i < borderLines.length; i++) {
                borderLines[i] = borderColour;
            }
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

    private static int binary(final String value) {
        return Integer.valueOf(value, 2);
    }

    public int[] getBorderLines() {
        return borderLines;
    }
}
