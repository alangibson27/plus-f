package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.InputStream;

public class Memory {

    private static final int PAGE_SIZE = 0x4000;
    private static final int DISPLAY_SIZE = 0x1b00;

    private static final int ROM_PAGE = 0;
    private static final int SCREEN_PAGE = 1;
    private static final int MIDDLE_PAGE = 2;
    private static final int HIGH_PAGE = 3;

    private Memory() {}

    private static Model currentModel;
    private static boolean memoryProtectionEnabled = true;
    private static boolean screenChanged = true;

    private static int[][] romPages;
    private static int[][] ramPages;
    private static int[] swapPage = new int[PAGE_SIZE];
    private static int[] displayMemory = new int[0x10000];

    private static int romPage;
    private static int screenPage;
    private static int highPage;

    private static final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public static void configure(final int[] addressableMemory, final Model model) throws IOException {
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
        highPage = model.highPage;
        currentModel = model;

        copyPageIntoMemory(addressableMemory, romPages[0], ROM_PAGE);
        memoryProtectionEnabled = true;
    }

    private static int[] readRom(final String romFileName) throws IOException {
        try (final InputStream is = Memory.class.getResourceAsStream(romFileName)) {
            final int[] rom = new int[PAGE_SIZE];
            int i = 0;
            for (int next = is.read(); next != -1; next = is.read()) {
                rom[i++] = next;
            }
            return rom;
        }
    }

    public static void setRomPage(final int[] memory, final int newRomPage) {
        if (romPage != newRomPage) {
            copyPageIntoMemory(memory, romPages[newRomPage], ROM_PAGE);
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
            if (newHighPage == 2) {
                copyMemoryIntoPage(memory, ramPages[2], MIDDLE_PAGE);
            } else if (newHighPage == 5) {
                copyMemoryIntoPage(memory, ramPages[5], SCREEN_PAGE);
            }
            copyMemoryIntoPage(memory, swapPage, HIGH_PAGE);
            copyPageIntoMemory(memory, ramPages[newHighPage], HIGH_PAGE);
            copyPageIntoPage(swapPage, ramPages[highPage]);

            highPage = newHighPage;
        }
    }

    private static void copyPageIntoMemory(final int[] memory, final int[] sourcePage, final int page) {
        System.arraycopy(sourcePage, 0x0000, memory, page * PAGE_SIZE, PAGE_SIZE);
    }

    private static void copyMemoryIntoPage(final int[] memory, final int[] targetPage, final int page) {
        System.arraycopy(memory, page * PAGE_SIZE, targetPage, 0x0000, PAGE_SIZE);
    }

    private static void copyPageIntoPage(final int[] sourcePage, final int[] targetPage) {
        System.arraycopy(sourcePage, 0x0000, targetPage, 0x0000, PAGE_SIZE);
    }

    public static void disableMemoryProtection() {
        memoryProtectionEnabled = false;
    }

    public static int[] getScreenBytes(final int[] memory) {
        if (currentModel == Model._48K || screenPage == 5) {
            return memory;
        } else {
            if (highPage == 7) {
                System.arraycopy(memory, HIGH_PAGE * PAGE_SIZE, displayMemory, SCREEN_PAGE * PAGE_SIZE, DISPLAY_SIZE);
            } else {
                System.arraycopy(ramPages[7], 0x0000, displayMemory, SCREEN_PAGE * PAGE_SIZE, DISPLAY_SIZE);
            }
            return displayMemory;
        }
    }

    public static void set(final int[] memory, final int addr, final int value) {
        final int page = addr >> 14;
        if (!memoryProtectionEnabled || page > 0) {
            final long offset = 16L + (addr * 4);
            final int prevValue = unsafe.getInt(memory, offset);
            if (prevValue != value) {
                unsafe.putInt(memory, offset, value);

                switch (currentModel) {
                    case _48K:
                        screenChanged = screenChanged || addr >= PAGE_SIZE && addr < 0x5b00;
                        break;

                    case PLUS_2:
                        switch (page) {
                            case 1:
                                screenChanged = screenChanged || (screenPage == 5 && addr >= PAGE_SIZE && addr < 0x5b00);
                                if (highPage == 5) {
                                    unsafe.putInt(memory, 16L + ((addr + 0x8000) * 4), value);
                                }
                                break;

                            case 2:
                                if (highPage == 2) {
                                    unsafe.putInt(memory, 16L + ((addr + PAGE_SIZE) * 4), value);
                                }

                            case 3:
                                screenChanged = screenChanged || (screenPage == highPage && addr >= HIGH_PAGE && addr < 0xdb00);
                                if (highPage == 2) {
                                    unsafe.putInt(memory, 16L + ((addr - PAGE_SIZE) * 4), value);
                                } else if (highPage == 5) {
                                    unsafe.putInt(memory, 16L + ((addr - 0x8000) * 4), value);
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
