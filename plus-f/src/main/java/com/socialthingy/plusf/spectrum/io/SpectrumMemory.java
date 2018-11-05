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
    private final int firstTickOfDisplay;
    private final int lastTickOfDisplay;
    protected boolean screenChanged = true;

    protected SpectrumMemory(final Clock clock, final Model model) {
        this.firstTickOfDisplay = 64 * model.ticksPerScanline;
        this.lastTickOfDisplay = (64 + 192) * model.ticksPerScanline;
        this.clock = clock;
        clock.setResetHandler(this::resetDisplayMemory);
    }

    protected abstract void resetDisplayMemory();

    protected void handleContention() {
        if (clock.getTicks() >= firstTickOfDisplay &&
                clock.getTicks() < lastTickOfDisplay) {
            clock.tick(2);
        }
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
        handleMemoryContention(addr >> 14);
        return super.get(addr);
    }

    protected abstract void handleMemoryContention(int page);

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
}
