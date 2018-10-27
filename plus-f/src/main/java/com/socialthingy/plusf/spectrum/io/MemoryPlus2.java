package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;

public class MemoryPlus2 extends SpectrumMemory {
    public static final int ROM_PAGE = 0;
    public static final int LOW_PAGE = 1;
    public static final int MIDDLE_PAGE = 2;
    public static final int HIGH_PAGE = 3;

    private final Clock clock;
    private boolean pagingDisabled = false;
    private int[][] romBanks;
    private int[][] ramBanks;
    private int[] swapBank = new int[SpectrumMemory.PAGE_SIZE];
    private int activeRomBank;
    private int activeScreenBank;
    private int activeHighBank;

    public MemoryPlus2(final Clock clock) {
        super(Model.PLUS_2, clock);
        this.clock = clock;

        romBanks = new int[Model.PLUS_2.romFileNames.length][];
        int pageIdx = 0;
        for (String romFileName: Model.PLUS_2.romFileNames) {
            romBanks[pageIdx++] = readRom(romFileName);
        }

        ramBanks = new int[Model.PLUS_2.ramPageCount][];
        for (int i = 0; i < Model.PLUS_2.ramPageCount; i++) {
            ramBanks[i] = new int[PAGE_SIZE];
        }

        activeRomBank = 0;
        activeScreenBank = Model.PLUS_2.screenPage;
        activeHighBank = Model.PLUS_2.highPage;
        copyBankIntoPage(romBanks[0], ROM_PAGE);
    }

    @Override
    protected void resetDisplayMemory() {
        screenChanged = true;
        if (activeScreenBank == 5) {
            System.arraycopy(addressableMemory, LOW_PAGE * PAGE_SIZE, displayMemory, 0x0000, 0x1b00);
        } else {
            System.arraycopy(addressableMemory, HIGH_PAGE * PAGE_SIZE, displayMemory, 0x0000, 0x1b00);
        }
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
            setActiveHighBank(newHighPage);
            setActiveScreenBank(newScreenPage);
            setActiveRomBank(newRomPage);

            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    @Override
    protected void handleMemoryContention(final int page) {
        if (clock.getTicks() >= firstTickOfDisplay &&
                clock.getTicks() < lastTickOfDisplay &&
                (page == 1 || (page == 3 && (activeHighBank & 1) == 1))) {
            clock.tick(2);
        }
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;
        handleMemoryContention(page);

        if (page != 0) {
            super.set(addr, value);

            switch (page) {
                case 1:
                    if (activeScreenBank == 5 && addr >= PAGE_SIZE && addr < 0x5b00) {
                        writeToDisplayIfBeforeScanlineReached(addr, value);
                    }

                    if (activeHighBank == 5) {
                        super.set(addr + PAGE_SIZE * 2, value);
                    }
                    break;

                case 2:
                    if (activeHighBank == 2) {
                        super.set(addr + PAGE_SIZE, value);
                    }

                case 3:
                    if (activeScreenBank == activeHighBank && addr >= HIGH_PAGE && addr < 0xdb00) {
                        writeToDisplayIfBeforeScanlineReached(addr, value);
                    }

                    if (activeHighBank == 2) {
                        super.set(addr - PAGE_SIZE, value);
                    } else if (activeHighBank == 5) {
                        super.set(addr - PAGE_SIZE * 2, value);
                    }
                    break;
            }
        }
    }

    void setActiveRomBank(final int newRomPage) {
        if (activeRomBank != newRomPage) {
            copyBankIntoPage(romBanks[newRomPage], ROM_PAGE);
            activeRomBank = newRomPage;
        }
    }

    void setActiveScreenBank(final int newScreenPage) {
        if (newScreenPage != 5 && newScreenPage != 7) {
            throw new IllegalArgumentException();
        }

        if (activeScreenBank != newScreenPage) {
            activeScreenBank = newScreenPage;
            screenChanged = true;
        }
    }

    void setActiveHighBank(final int newHighPageInMemory) {
        if (activeHighBank != newHighPageInMemory) {
            if (newHighPageInMemory == 2) {
                copyPageIntoBank(MIDDLE_PAGE, ramBanks[2]);
            } else if (newHighPageInMemory == 5) {
                copyPageIntoBank(LOW_PAGE, ramBanks[5]);
            }
            copyPageIntoBank(HIGH_PAGE, swapBank);
            copyBankIntoPage(ramBanks[newHighPageInMemory], HIGH_PAGE);
            copyBankIntoBank(swapBank, ramBanks[activeHighBank]);

            activeHighBank = newHighPageInMemory;
        }
    }

    private void copyPageIntoBank(final int page, final int[] targetBank) {
        copyFrom(page * PAGE_SIZE, targetBank);
    }

    private void copyBankIntoBank(final int[] sourceBank, final int[] targetBank) {
        System.arraycopy(sourceBank, 0x0000, targetBank, 0x0000, PAGE_SIZE);
    }

    public void copyIntoBank(final int[] source, final int targetBank) {
        copyBankIntoBank(source, ramBanks[targetBank]);
    }

    @Override
    public void copyIntoPage(final int[] source, final int destination) {
        super.copyIntoPage(source, destination);

        final int pageInMemory = destination >> 14;
        final int ramBankNumber;
        switch (pageInMemory) {
            case LOW_PAGE:
                ramBankNumber = activeScreenBank;
                break;

            case MIDDLE_PAGE:
                ramBankNumber = 2;
                break;

            default:
                ramBankNumber = activeHighBank;
                break;
        }

        System.arraycopy(source, 0, ramBanks[ramBankNumber], 0, source.length);
    }
}
