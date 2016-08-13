package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.spectrum.display.Display;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;

public class ULA implements IO {
    private final TapePlayer tapePlayer;
    private final Keyboard keyboard;
    private final Display display;
    private final int[] memory;

    private boolean pagingDisabled = false;
    private int earBit;
    private int currentCycleTstates;

    public ULA(final Display display, final Keyboard keyboard, final TapePlayer tapePlayer, final int[] memory) {
        this.display = display;
        this.keyboard = keyboard;
        this.tapePlayer = tapePlayer;
        this.memory = memory;
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

        if (port == 0xfd && accumulator == 0x7f && !pagingDisabled) {
            final int newHighPage = value & 0b00000111;
            final int newScreenPage = (value & 0b00001000) == 0 ? 5 : 7;
            final int newRomPage = (value & 0b00010000) == 0 ? 0 : 1;
            Memory.setHighPage(memory, newHighPage);
            Memory.setScreenPage(newScreenPage);
            Memory.setRomPage(memory, newRomPage);

            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    public void newCycle() {
        currentCycleTstates = 0;
    }

    public void advanceCycle(final int tstates) {
        currentCycleTstates += tstates;
        if (tapePlayer.playingProperty().get()) {
            earBit = tapePlayer.skip(tstates) ? 1 << 6 : 0;
        }
    }
}
