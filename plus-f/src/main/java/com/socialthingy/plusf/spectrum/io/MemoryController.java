package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;

public class MemoryController implements IO {
    private boolean pagingDisabled = false;
    private final int[] memory;

    public MemoryController(final int[] memory) {
        this.memory = memory;
    }

    @Override
    public boolean recognises(int low, int high) {
        return (high & 0b10000000) == 0 && (low & 0b10) == 0;
    }

    @Override
    public int read(int low, int high) {
        return 0;
    }

    @Override
    public void write(int low, int high, int value) {
        if (!pagingDisabled) {
            final int newHighPage = value & 0b00000111;
            final int newScreenPage = (value & 0b00001000) == 0 ? 5 : 7;
            final int newRomPage = (value & 0b00010000) == 0 ? 0 : 1;
            Memory.setHighPage(memory, newHighPage);
            Memory.setScreenPage(newScreenPage);
            Memory.setRomPage(memory, newRomPage);

            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    public void reset(final Model model) {
        pagingDisabled = model == Model._48K;
    }
}
