package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;

public class Memory48K extends SpectrumMemory {
    private static final int PAGE_SIZE = 0x4000;
    private static final int ROM_PAGE = 0;

    public Memory48K(final Clock clock) {
        super(Model._48K, clock);
        copyBankIntoPage(readRom(Model._48K.romFileNames[0]), ROM_PAGE);
    }

    protected void resetDisplayMemory() {
        screenChanged = true;
        System.arraycopy(addressableMemory, 0x4000, displayMemory, 0x0000, 0x1b00);
    }

    @Override
    protected void handleMemoryContention(final int page) {
        if (clock.getTicks() >= firstTickOfDisplay &&
                clock.getTicks() < lastTickOfDisplay && page == 1) {
            clock.tick(2);
        }
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;
        handleMemoryContention(page);

        if (page != ROM_PAGE) {
            super.set(addr, value);
            if (addr >= PAGE_SIZE && addr < 0x5b00) {
                writeToDisplayIfBeforeScanlineReached(addr, value);
            }
        }
    }
}
