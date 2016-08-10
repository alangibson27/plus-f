package com.socialthingy.plusf.spectrum.remote;

import com.google.common.primitives.Ints;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.socialthingy.plusf.spectrum.display.Display.SCREEN_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay.BORDER;

public class EmulatorState {
    public static final int BORDER_LINE_COUNT = SCREEN_HEIGHT + (2 * BORDER);

    private int[] memory;
    private int[] borderLines;
    private boolean flashActive;

    public EmulatorState(final int[] memory, final int[] borderLines, final boolean flashActive) {
        this.memory = memory;
        this.borderLines = borderLines;
        this.flashActive = flashActive;
    }

    public int[] getMemory() {
        return memory;
    }

    public int[] getBorderLines() {
        return borderLines;
    }

    public boolean isFlashActive() {
        return flashActive;
    }

    public static EmulatorState deserialise(final InputStream in) {
        try {
            final int[] memory = new int[0x10000];
            for (int i = 0x4000; i < 0x5b00; i++) {
                memory[i] = in.read();
            }

            final int[] borderLines = new int[BORDER_LINE_COUNT];
            for (int i = 0; i < BORDER_LINE_COUNT; i++) {
                borderLines[i] = Ints.fromBytes(
                    (byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()
                );
            }

            return new EmulatorState(memory, borderLines, in.read() != 0);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }

    public static void serialise(final Pair<EmulatorState, OutputStream> stateAndStream) {
        final EmulatorState state = stateAndStream.getKey();
        final OutputStream out = stateAndStream.getValue();

        try {
            final int[] memory = state.getMemory();
            for (int i = 0x4000; i < 0x5b00; i++) {
                out.write(memory[i]);
            }

            final int[] borderLines = state.getBorderLines();
            for (int i = 0; i < BORDER_LINE_COUNT; i++) {
                out.write(Ints.toByteArray(borderLines[i]));
            }

            out.write(state.isFlashActive() ? 1 : 0);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
