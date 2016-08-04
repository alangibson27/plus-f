package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.spectrum.display.Display;
import com.socialthingy.plusf.z80.IO;

import java.util.*;

public class ULA implements IO {

    private static final int[] KEY_POSITIONS = new int[] {
        binary("11110"),
        binary("11101"),
        binary("11011"),
        binary("10111"),
        binary("01111")
    };


    private final Set<Character> keysDown = new HashSet<>();
    private final Map<Character, Integer>[] halfRows = new Map[8];

    private int earBit;
    private int currentCycleTstates;
    private TapePlayer tapePlayer;
    private final Display display;

    public ULA(final Display display, final TapePlayer tapePlayer) {
        this.display = display;
        this.tapePlayer = tapePlayer;

        halfRows[0] = buildHalfRow("^zxcv");
        halfRows[1] = buildHalfRow("asdfg");
        halfRows[2] = buildHalfRow("qwert");
        halfRows[3] = buildHalfRow("12345");
        halfRows[4] = buildHalfRow("09876");
        halfRows[5] = buildHalfRow("poiuy");
        halfRows[6] = buildHalfRow("_lkjh");
        halfRows[7] = buildHalfRow(" $mnb");
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
                return binary("10111111") | earBit;
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

                return binary("10100000") | earBit | bits;
            }
        }
        return 0;
    }

    @Override
    public void write(int port, int accumulator, int value) {
        if (port == 0xfe) {
            display.changeBorder(currentCycleTstates, value);
        }
    }

    public void newCycle() {
        currentCycleTstates = 0;
    }

    public void advanceCycle(final int tstates) {
        currentCycleTstates += tstates;
        for (int i = 0; i < tstates; i++) {
            if (tapePlayer.hasNext()) {
                earIn(tapePlayer.next());
            }
        }
    }

    public void earIn(final boolean high) {
        this.earBit = high ? 1 << 6 : 0;
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

}
