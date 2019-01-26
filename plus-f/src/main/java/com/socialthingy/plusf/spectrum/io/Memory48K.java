package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;

public class Memory48K extends SpectrumMemory {
    private static final int PAGE_SIZE = 0x4000;
    private static final int ROM_PAGE = 0;

    public Memory48K() {
        super(Model._48K);
        copyBankIntoPage(readRom(Model._48K.romFileNames[0]), ROM_PAGE);
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;

        if (page != ROM_PAGE) {
            super.set(addr, value);
            if (addr >= PAGE_SIZE && addr < 0x5b00) {
                writeToDisplayMemory(addr, value);
            }
        }
    }

    public void copyIntoPage(final int[] source, final int destination) {
        copyInto(source, destination * PAGE_SIZE);
    }

    @Override
    public void copyFromBank(final int sourceBank, final int[] target) {
        switch (sourceBank) {
            case 5:
                System.arraycopy(addressableMemory, 0x4000, target, 0, PAGE_SIZE);
                break;

            case 1:
                System.arraycopy(addressableMemory, 0x8000, target, 0, PAGE_SIZE);
                break;

            case 2:
                System.arraycopy(addressableMemory, 0xc000, target, 0, PAGE_SIZE);
                break;
        }
    }

    @Override
    public void resetDisplayMemory() {
        System.arraycopy(addressableMemory, 0x4000, displayMemory, 0x0000, 0x1b00);
    }
}
