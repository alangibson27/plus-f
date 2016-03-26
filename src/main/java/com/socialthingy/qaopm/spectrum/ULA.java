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

    public static final int SCREEN_HEIGHT = 192;
    public static final int TOP_BORDER_HEIGHT = 64;
    public static final int BOTTOM_BORDER_HEIGHT = 56;

    private final List<int[]> borderChanges = new ArrayList<>();
    private final int[] borderLines;
    private final Set<Character> keysDown = new HashSet<>();
    private final Map<Character, Integer>[] halfRows = new Map[8];
    private final Computer computer;
    private final int fullDisplayStart;
    private final int fullDisplayEnd;

    private int initialBorderColour;

    public ULA(final Computer computer, final int displayedTopBorder, final int displayedBottomBorder) {
        this.computer = computer;
        borderLines = new int[displayedTopBorder + SCREEN_HEIGHT + displayedBottomBorder];
        fullDisplayStart = TOP_BORDER_HEIGHT - displayedTopBorder;
        fullDisplayEnd = TOP_BORDER_HEIGHT + SCREEN_HEIGHT + displayedBottomBorder;

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
            borderChanges.add(new int[] {computer.getCurrentCycleTstates(), value & 0b111});
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
        if (!borderChanges.isEmpty()) {
            final int[] firstChange = borderChanges.get(0);
            for (int j = 0; j < firstChange[0] / 224; j++) {
                setBorderLine(j, initialBorderColour);
            }

            for (int i = 0; i < borderChanges.size() - 1; i++) {
                final int[] change1 = borderChanges.get(i);
                final int[] change2 = borderChanges.get(i + 1);

                final int blockStart = change1[0] / 224;
                final int blockEnd = change2[0] / 224;

                final int colour = 0xff000000 | dullColour(change1[1]);
                for (int j = blockStart; j < blockEnd; j++) {
                    setBorderLine(j, colour);
                }
            }

            final int[] finalChange = borderChanges.get(borderChanges.size() - 1);
            final int colour = 0xff000000 | dullColour(finalChange[1]);
            for (int j = finalChange[0] / 224; j < borderLines.length; j++) {
                setBorderLine(j, colour);
            }
            initialBorderColour = colour;
            borderChanges.clear();
        } else {
            for (int i = 0; i < borderLines.length; i++) {
                borderLines[i] = initialBorderColour;
            }
        }

        return borderLines;
    }

    private void setBorderLine(final int line, final int colourId) {
        if (line >= fullDisplayStart && line < fullDisplayEnd) {
            borderLines[line - fullDisplayStart] = colourId;
        }
    }
}
