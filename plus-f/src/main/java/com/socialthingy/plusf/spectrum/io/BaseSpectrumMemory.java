package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.SimpleMemory;

import java.io.IOException;
import java.io.InputStream;

abstract class BaseSpectrumMemory extends SimpleMemory implements IO {
    private static final int SCANLINES_BEFORE_DISPLAY = 64;
    protected static final int PAGE_SIZE = 0x4000;

    protected final Clock clock;
    protected int[] displayMemory = new int[0x1b00];
    private final int ticksPerScanline;
    protected final int firstTickOfDisplay;
    protected final int lastTickOfDisplay;
    protected boolean screenChanged = true;

    protected BaseSpectrumMemory(final Model model, final Clock clock) {
        ticksPerScanline = model.ticksPerScanline;
        firstTickOfDisplay = 64 * ticksPerScanline;
        lastTickOfDisplay = (64 + 192) * ticksPerScanline;
        this.clock = clock;
    }

    protected abstract void resetDisplayMemory();

    protected int[] getDisplayMemory() {
        return displayMemory;
    }

    @Override
    public boolean recognises(int low, int high) {
        return false;
    }

    @Override
    public int read(int low, int high) {
        return 0;
    }

    @Override
    public void write(int low, int high, int value) {}

    protected int yCoord(final int addr) {
        final int hi = addr >> 8;
        final int lo = addr & 0xff;
        return ((hi & 24) << 2) + ((lo & 224) >> 2) + (hi & 7);
    }

    protected int[] readRom(final String romFileName) {
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

    @Override
    public int get(final int addr) {
        handleMemoryContention(addr >> 14);
        return super.get(addr);
    }

    protected abstract void handleMemoryContention(int page);

    protected void writeToDisplayIfBeforeScanlineReached(int addr, final int value) {
        addr &= 0x3fff;
        if (clock.getTicks() < (SCANLINES_BEFORE_DISPLAY + yCoord(addr)) * ticksPerScanline) {
            screenChanged = true;
            displayMemory[addr] = value;
        }
    }

    protected void copyBankIntoPage(final int[] sourceRamPage, final int pageInMemory) {
        copyInto(sourceRamPage, pageInMemory * PAGE_SIZE);
    }

    protected boolean screenChanged() {
        return screenChanged;
    }

    protected void markScreenDrawn() {
        screenChanged = false;
    }

    protected void copyIntoPage(final int[] source, final int destination) {
        copyInto(source, destination * PAGE_SIZE);
    }
}
