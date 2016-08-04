package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.spectrum.display.Display;
import com.socialthingy.plusf.z80.IO;

public class ULA implements IO {
    private final TapePlayer tapePlayer;
    private final Keyboard keyboard;
    private final Display display;

    private int earBit;
    private int currentCycleTstates;

    public ULA(final Display display, final Keyboard keyboard, final TapePlayer tapePlayer) {
        this.display = display;
        this.keyboard = keyboard;
        this.tapePlayer = tapePlayer;
    }

    @Override
    public int read(int port, int accumulator) {
        if (port == 0xfe) {
            return keyboard.readKeyboard(accumulator) | earBit;
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
                this.earBit = tapePlayer.next() ? 1 << 6 : 0;
            }
        }
    }
}
