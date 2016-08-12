package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.spectrum.Model;

import java.io.IOException;
import java.io.InputStream;

public class Memory {
    private Memory() {}

    private static Model currentModel;
    private static boolean memoryProtectionEnabled = true;
    private static boolean screenChanged = true;

    private static int[][] romPages;
    private static int[][] ramPages;
    private static int[] swapPage = new int[0x4000];
    private static int[] displayMemory = new int[0x10000];

    private static int romPage;
    private static int screenPage;
    private static int midPage;
    private static int highPage;

    public static int[] configure(final Model model) throws IOException {
        final int[] addressableMemory = new int[0x10000];
        romPages = new int[model.romFileNames.length][];
        int pageIdx = 0;
        for (String romFileName: model.romFileNames) {
            romPages[pageIdx++] = readRom(romFileName);
        }

        ramPages = new int[model.ramPageCount][];
        for (int i = 0; i < model.ramPageCount; i++) {
            ramPages[i] = new int[0x4000];
        }

        romPage = 0;
        screenPage = model.screenPage;
        midPage = model.midPage;
        highPage = model.highPage;
        currentModel = model;

        System.arraycopy(romPages[0], 0, addressableMemory, 0, 0x4000);
        return addressableMemory;
    }

    private static final int[] readRom(final String romFileName) throws IOException {
        try (final InputStream is = Memory.class.getResourceAsStream(romFileName)) {
            final int[] rom = new int[0x4000];
            int i = 0;
            for (int next = is.read(); next != -1; next = is.read()) {
                rom[i++] = next;
            }
            return rom;
        }
    }

    public static void setRomPage(final int[] memory, final int newRomPage) {
        if (romPage != newRomPage) {
            System.arraycopy(memory, 0x0000, swapPage, 0, 0x4000);
            System.arraycopy(romPages[newRomPage], 0x0000, memory, 0x0000, 0x4000);
            System.arraycopy(swapPage, 0x0000, romPages[romPage], 0x0000, 0x4000);

            romPage = newRomPage;
        }
    }

    public static void setScreenPage(final int newScreenPage) {
        if (newScreenPage != 5 && newScreenPage != 7) {
            throw new IllegalArgumentException();
        }

        if (screenPage != newScreenPage) {
            screenPage = newScreenPage;
            screenChanged = true;
        }
    }

    public static void setHighPage(final int[] memory, final int newHighPage) {
        if (highPage != newHighPage) {
            System.arraycopy(memory, 0xc000, swapPage, 0, 0x4000);
            System.arraycopy(ramPages[newHighPage], 0x0000, memory, 0xc000, 0x4000);
            System.arraycopy(swapPage, 0x0000, ramPages[highPage], 0x0000, 0x4000);

            highPage = newHighPage;
        }
    }

    public static void disableMemoryProtection() {
        memoryProtectionEnabled = false;
    }

    public static int[] getScreenBytes(final int[] memory) {
        if (currentModel == Model._48K || screenPage == 5) {
            return memory;
        } else {
            if (highPage == 7) {
                System.arraycopy(memory, 0xc000, displayMemory, 0x4000, 0x1b00);
            } else {
                System.arraycopy(ramPages[7], 0x0000, displayMemory, 0x4000, 0x1b00);
            }
            return displayMemory;
        }
    }

    public static void set(final int[] memory, final int addr, final int value) {
        final int page = addr >> 14;
        if (!memoryProtectionEnabled || page > 0) {
            if (memory[addr] != value) {
                memory[addr] = value;

                switch (currentModel) {
                    case _48K:
                        screenChanged = screenChanged || addr >= 0x4000 && addr < 0x5b00;
                        break;

                    case PLUS_2:
                        switch (page) {
                            case 1:
                                screenChanged = screenChanged || (screenPage == 5 && addr >= 0x4000 && addr < 0x5b00);
                                if (highPage == 5) {
                                    memory[addr + 0x8000] = value;
                                }
                                break;

                            case 3:
                                screenChanged = screenChanged || (screenPage == highPage && addr >= 0xc000 && addr < 0xdb00);
                                if (highPage == 5) {
                                    memory[addr - 0x8000] = value;
                                }
                                break;
                        }
                }
            }
        }
    }

    public static boolean screenChanged() {
        return screenChanged;
    }

    public static void markScreenDrawn() {
        screenChanged = false;
    }
}
