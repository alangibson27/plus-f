package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.UncontendedMemory;

import java.io.IOException;
import java.io.InputStream;

public abstract class SpectrumMemory extends UncontendedMemory implements IO {
    protected static final int PAGE_SIZE = 0x4000;

    protected int[] displayMemory = new int[0x1b00];
    protected final int firstTickOfDisplay;
    protected final int lastTickOfDisplay;
    protected final int ticksPerScanline;
    protected boolean screenChanged = true;

    protected SpectrumMemory(final Model model) {
        super();
        this.firstTickOfDisplay = model.scanlinesBeforeDisplay * model.ticksPerScanline;
        this.lastTickOfDisplay = (model.scanlinesBeforeDisplay + 192) * model.ticksPerScanline;
        this.ticksPerScanline = model.ticksPerScanline;
    }

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
        return super.get(addr);
    }

    protected void writeToDisplayMemory(int addr, final int value) {
        addr &= 0x3fff;
        displayMemory[addr] = value;
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

    public boolean contendedAddress(final int addr) {
        return addr >> 14 == 1;
    }

    public abstract void resetDisplayMemory();
}
