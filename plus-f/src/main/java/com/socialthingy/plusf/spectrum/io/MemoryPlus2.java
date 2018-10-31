package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;

public class MemoryPlus2 extends SpectrumMemory {
    public static final int ROM_PAGE = 0;
    public static final int LOW_PAGE = 1;
    public static final int MIDDLE_PAGE = 2;
    public static final int HIGH_PAGE = 3;

    private boolean pagingDisabled = false;
    private int[][] romBanks;
    private int[][] ramBanks;
    private int activeRomBank;
    private int activeScreenBank;
    private int activeHighBank;

    public MemoryPlus2(final ULA ula, final Clock clock) {
        super(ula, clock, Model.PLUS_2);

        romBanks = new int[Model.PLUS_2.romFileNames.length][];
        int pageIdx = 0;
        for (String romFileName : Model.PLUS_2.romFileNames) {
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
        System.arraycopy(ramBanks[activeScreenBank], 0, displayMemory, 0, 0x1b00);
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
            activeHighBank = newHighPage;
            activeScreenBank = newScreenPage;
            activeRomBank = newRomPage;
            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    @Override
    protected void handleMemoryContention(final int page) {
        if (page == 1 || page == 3 && (activeHighBank & 1) == 1) {
            ula.handleContention();
        }
    }

    @Override
    public int get(int addr) {
        addr &= 0xffff;
        final int page = addr >> 14;
        final int offsetInPage = addr & 0x3fff;
        handleMemoryContention(page);

        switch (page) {
            case ROM_PAGE:
                return romBanks[activeRomBank][offsetInPage];
            case LOW_PAGE:
                return ramBanks[5][offsetInPage];
            case MIDDLE_PAGE:
                return ramBanks[2][offsetInPage];
            default:
                return ramBanks[activeHighBank][offsetInPage];
        }
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;
        final int offsetInPage = addr & 0x3fff;
        handleMemoryContention(page);

        switch (page) {
            case LOW_PAGE:
                ramBanks[5][offsetInPage] = value;
                if (activeScreenBank == 5 && offsetInPage < 0x1b00) {
                    writeToDisplayIfBeforeScanlineReached(addr, value);
                }
                break;
            case MIDDLE_PAGE:
                ramBanks[2][offsetInPage] = value;
                break;
            case HIGH_PAGE:
                ramBanks[activeHighBank][offsetInPage] = value;
                if (activeScreenBank == activeHighBank && offsetInPage < 0x1b00) {
                    writeToDisplayIfBeforeScanlineReached(addr, value);
                }
                break;
        }
    }

    void setActiveRomBank(final int newRomPage) {
        activeRomBank = newRomPage;
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
        activeHighBank = newHighPageInMemory;
    }

    public void copyIntoBank(final int[] source, final int targetBank) {
        System.arraycopy(source, 0x0000, ramBanks[targetBank], 0x0000, PAGE_SIZE);
    }
}