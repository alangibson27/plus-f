package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;

import java.io.IOException;
import java.io.InputStream;

public class SpectrumMemory extends Memory implements IO {
    private static final int PAGE_SIZE = 0x4000;
    private static final int ROM_PAGE = 0;
    private static final int SCREEN_PAGE = 1;
    private static final int MIDDLE_PAGE = 2;
    private static final int HIGH_PAGE = 3;
    private static final int SCANLINES_BEFORE_DISPLAY = 64;

    private final Clock clock;
    private boolean pagingDisabled = false;
    private Model currentModel;
    private boolean screenChanged = true;
    private int[][] romPages;
    private int[][] ramPages;
    private int[] swapPage = new int[SpectrumMemory.PAGE_SIZE];
    private int[] displayMemory = new int[0x10000];
    private int romPage;
    private int screenPage;
    private int highPageInMemory;

    public SpectrumMemory(final Clock clock) {
        this.clock = clock;
        clock.addResetHandler(this::resetDisplayMemory);
    }

    public void setDisplayMemoryDirectly(final int[] src, final int addr, final int len) {
        System.arraycopy(src, addr, displayMemory, addr, len);
    }

    private void resetDisplayMemory() {
        screenChanged = true;
        if (currentModel == Model._48K) {
            System.arraycopy(addressableMemory, 0x4000, displayMemory, 0x4000, 0x1b00);
        } else {
            if (screenPage == 5) {
                System.arraycopy(addressableMemory, 0x4000, displayMemory, 0x4000, 0x1b00);
            } else {
                System.arraycopy(addressableMemory, HIGH_PAGE * PAGE_SIZE, displayMemory, 0x4000, 0x1b00);
            }
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
            setHighPageInMemory(newHighPage);
            setScreenPage(newScreenPage);
            setRomPage(newRomPage);

            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    @Override
    public int get(final int addr) {
        if (addr >= PAGE_SIZE && addr < 0x8000 &&
                clock.getTicks() >= 64 * currentModel.ticksPerScanline &&
                clock.getTicks() < (64 + 192) * currentModel.ticksPerScanline) {
            clock.tick(2);
        }

        return super.get(addr);
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;

        if (page > 0) {
            final int prevValue = super.get(addr);
            if (prevValue != value) {
                super.set(addr, value);

                switch (currentModel) {
                    case _48K:
                        if (addr >= PAGE_SIZE && addr < 0x8000 &&
                                clock.getTicks() >= 64 * currentModel.ticksPerScanline &&
                                clock.getTicks() < (64 + 192) * currentModel.ticksPerScanline) {
                            clock.tick(2);
                        }

                        if (addr >= SpectrumMemory.PAGE_SIZE && addr < 0x5b00) {
                            writeToDisplayIfBeforeScanlineReached(addr, value);
                        }
                        break;

                    case PLUS_2:
                        switch (page) {
                            case 1:
                                if (screenPage == 5 && addr >= SpectrumMemory.PAGE_SIZE && addr < 0x5b00) {
                                    writeToDisplayIfBeforeScanlineReached(addr, value);
                                }

                                if (highPageInMemory == 5) {
                                    super.set(addr + 0x8000, value);
                                }
                                break;

                            case 2:
                                if (highPageInMemory == 2) {
                                    super.set(addr + SpectrumMemory.PAGE_SIZE, value);
                                }

                            case 3:
                                if (screenPage == highPageInMemory && addr >= SpectrumMemory.HIGH_PAGE && addr < 0xdb00) {
                                    writeToDisplayIfBeforeScanlineReached(addr, value);
                                }

                                if (highPageInMemory == 2) {
                                    super.set(addr - SpectrumMemory.PAGE_SIZE, value);
                                } else if (highPageInMemory == 5) {
                                    super.set(addr - 0x8000, value);
                                }
                                break;
                        }
                }
            }
        }
    }

    private void writeToDisplayIfBeforeScanlineReached(final int addr, final int value) {
        if (clock.getTicks() < (SCANLINES_BEFORE_DISPLAY + yCoord(addr)) * currentModel.ticksPerScanline) {
            screenChanged = true;
            displayMemory[addr] = value;
        }
    }

    private int yCoord(final int addr) {
        final int hi = addr >> 8;
        final int lo = addr & 0xff;
        return ((hi & 24) << 2) + ((lo & 224) >> 2) + (hi & 7);
    }

    public void configure(final Model model) {
        pagingDisabled = model == Model._48K;
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
        copyInto(sourceRamPage, pageInMemory * PAGE_SIZE);
    }

    private void copyMemoryIntoRamPage(final int pageInMemory, final int[] targetRamPage) {
        copyFrom(pageInMemory * PAGE_SIZE, targetRamPage);
    }

    private void copyRamPageIntoRamPage(final int[] sourceRamPage, final int[] targetRamPage) {
        System.arraycopy(sourceRamPage, 0x0000, targetRamPage, 0x0000, PAGE_SIZE);
    }

    public int[] getScreenBytes() {
        return displayMemory;
    }

    public boolean screenChanged() {
        return screenChanged;
    }

    public void markScreenDrawn() {
        screenChanged = false;
    }

    public void copyIntoPage(final int[] source, final int destination) {
        copyInto(source, destination * PAGE_SIZE);

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
