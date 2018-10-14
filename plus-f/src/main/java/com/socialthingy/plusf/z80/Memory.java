package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.spectrum.Model;

import java.io.IOException;
import java.io.InputStream;

public class Memory {

    private static final int PAGE_SIZE = 0x4000;
    private static final int DISPLAY_SIZE = 0x1b00;

    private static final int ROM_PAGE = 0;
    private static final int SCREEN_PAGE = 1;
    private static final int MIDDLE_PAGE = 2;
    private static final int HIGH_PAGE = 3;

    private Model currentModel;
    private boolean memoryProtectionEnabled = true;
    private boolean screenChanged = true;

    private final int[] addressableMemory;
    private int[][] romPages;
    private int[][] ramPages;
    private int[] swapPage = new int[PAGE_SIZE];
    private int[] displayMemory = new int[0x10000];

    private int romPage;
    private int screenPage;
    private int highPageInMemory;

    public Memory() {
        this.addressableMemory = new int[0x10000];
        this.currentModel = Model._48K;
    }

    public Memory(final boolean memoryProtectionEnabled) {
        this();
        this.memoryProtectionEnabled = memoryProtectionEnabled;
    }

    public void configure(final Model model) {
        romPages = new int[model.romFileNames.length][];
        int pageIdx = 0;
        for (String romFileName: model.romFileNames) {
            romPages[pageIdx++] = readRom(romFileName);
        }

        ramPages = new int[model.ramPageCount][];
        for (int i = 0; i < model.ramPageCount; i++) {
            ramPages[i] = new int[PAGE_SIZE];
        }

        romPage = 0;
        screenPage = model.screenPage;
        highPageInMemory = model.highPage;
        currentModel = model;

        copyRamPageIntoMemory(romPages[0], ROM_PAGE);
        memoryProtectionEnabled = true;
    }

    private int[] readRom(final String romFileName) {
        try (final InputStream is = Memory.class.getResourceAsStream(romFileName)) {
            final int[] rom = new int[PAGE_SIZE];
            int i = 0;
            for (int next = is.read(); next != -1; next = is.read()) {
                rom[i++] = next;
            }
            return rom;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setRomPage(final int newRomPage) {
        if (romPage != newRomPage) {
            copyRamPageIntoMemory(romPages[newRomPage], ROM_PAGE);
            romPage = newRomPage;
        }
    }

    public void setScreenPage(final int newScreenPage) {
        if (newScreenPage != 5 && newScreenPage != 7) {
            throw new IllegalArgumentException();
        }

        if (screenPage != newScreenPage) {
            screenPage = newScreenPage;
            screenChanged = true;
        }
    }

    public void setHighPageInMemory(final int newHighPageInMemory) {
        if (highPageInMemory != newHighPageInMemory) {
            if (newHighPageInMemory == 2) {
                copyMemoryIntoRamPage(MIDDLE_PAGE, ramPages[2]);
            } else if (newHighPageInMemory == 5) {
                copyMemoryIntoRamPage(SCREEN_PAGE, ramPages[5]);
            }
            copyMemoryIntoRamPage(HIGH_PAGE, swapPage);
            copyRamPageIntoMemory(ramPages[newHighPageInMemory], HIGH_PAGE);
            copyRamPageIntoRamPage(swapPage, ramPages[highPageInMemory]);

            highPageInMemory = newHighPageInMemory;
        }
    }

    private void copyRamPageIntoMemory(final int[] sourceRamPage, final int pageInMemory) {
        System.arraycopy(sourceRamPage, 0x0000, addressableMemory, pageInMemory * PAGE_SIZE, PAGE_SIZE);
    }

    private void copyMemoryIntoRamPage(final int pageInMemory, final int[] targetRamPage) {
        System.arraycopy(addressableMemory, pageInMemory * PAGE_SIZE, targetRamPage, 0x0000, PAGE_SIZE);
    }

    private void copyRamPageIntoRamPage(final int[] sourceRamPage, final int[] targetRamPage) {
        System.arraycopy(sourceRamPage, 0x0000, targetRamPage, 0x0000, PAGE_SIZE);
    }

    public int[] getScreenBytes() {
        if (currentModel == Model._48K || screenPage == 5) {
            return addressableMemory;
        } else {
            if (highPageInMemory == 7) {
                System.arraycopy(addressableMemory, HIGH_PAGE * PAGE_SIZE, displayMemory, SCREEN_PAGE * PAGE_SIZE, DISPLAY_SIZE);
            } else {
                System.arraycopy(ramPages[7], 0x0000, displayMemory, SCREEN_PAGE * PAGE_SIZE, DISPLAY_SIZE);
            }
            return displayMemory;
        }
    }

    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;
        if (!memoryProtectionEnabled || page > 0) {
            final int prevValue = addressableMemory[addr];
            if (prevValue != value) {
                addressableMemory[addr] = value;

                switch (currentModel) {
                    case _48K:
                        screenChanged = screenChanged || addr >= PAGE_SIZE && addr < 0x5b00;
                        break;

                    case PLUS_2:
                        switch (page) {
                            case 1:
                                screenChanged = screenChanged || (screenPage == 5 && addr >= PAGE_SIZE && addr < 0x5b00);
                                if (highPageInMemory == 5) {
                                    addressableMemory[addr + 0x8000] = value;
                                }
                                break;

                            case 2:
                                if (highPageInMemory == 2) {
                                    addressableMemory[addr + PAGE_SIZE] = value;
                                }

                            case 3:
                                screenChanged = screenChanged || (screenPage == highPageInMemory && addr >= HIGH_PAGE && addr < 0xdb00);
                                if (highPageInMemory == 2) {
                                    addressableMemory[addr - PAGE_SIZE] = value;
                                } else if (highPageInMemory == 5) {
                                    addressableMemory[addr - 0x8000] = value;
                                }
                                break;
                        }
                }
            }
        }
    }

    public int get(final int addr) {
        return addressableMemory[addr & 0xffff];
    }

    public boolean screenChanged() {
        return screenChanged;
    }

    public void markScreenDrawn() {
        screenChanged = false;
    }

    public void copyFrom(final int[] source, final int destination) {
        System.arraycopy(source, 0, addressableMemory, destination * PAGE_SIZE, PAGE_SIZE);

        if (currentModel == Model.PLUS_2) {
            final int pageInMemory = destination >> 14;
            final int ramPageNumber;
            switch (pageInMemory) {
                case SCREEN_PAGE:
                    ramPageNumber = screenPage;
                    break;

                case MIDDLE_PAGE:
                    ramPageNumber = 2;
                    break;

                default:
                    ramPageNumber = highPageInMemory;
                    break;
            }

            System.arraycopy(source, 0, ramPages[ramPageNumber], 0, source.length);
        }
    }
}
