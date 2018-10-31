package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.SimpleMemory;

import java.io.IOException;
import java.io.InputStream;

public abstract class SpectrumMemory extends SimpleMemory implements IO {
    protected static final int PAGE_SIZE = 0x4000;

    protected final Clock clock;
    protected int[] displayMemory = new int[0x1b00];
    private final int ticksPerScanline;
    private final int scanlinesBeforeDisplay;
    protected boolean screenChanged = true;
    protected final ULA ula;

    protected SpectrumMemory(final ULA ula, final Clock clock, final Model model) {
        this.ula = ula;
        this.clock = clock;
        this.ticksPerScanline = model.ticksPerScanline;
        this.scanlinesBeforeDisplay = model.scanlinesBeforeDisplay;
        clock.setResetHandler(this::resetDisplayMemory);
    }

    protected abstract void resetDisplayMemory();

    public int[] getDisplayMemory() {
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
        if (addr < 6912 && clock.getTicks() < (scanlinesBeforeDisplay + yCoord(addr)) * ticksPerScanline) {
            screenChanged = true;
            displayMemory[addr] = value;
        }
    }

    protected void copyBankIntoPage(final int[] sourceRamPage, final int pageInMemory) {
        copyInto(sourceRamPage, pageInMemory * PAGE_SIZE);
    }

    public boolean screenChanged() {
        return screenChanged;
    }

    public void markScreenDrawn() {
        screenChanged = false;
    }

    public void copyIntoPage(final int[] source, final int destination) {
        copyInto(source, destination * PAGE_SIZE);
    }
}
