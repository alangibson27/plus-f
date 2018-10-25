package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;

public class SwitchableMemory implements Memory, IO {
    private final Clock clock;
    private BaseSpectrumMemory activeMemory;

    public SwitchableMemory(final Clock clock) {
        this.clock = clock;
        clock.addResetHandler(this::resetDisplayMemory);
    }

    private void resetDisplayMemory() {
        activeMemory.resetDisplayMemory();
    }

    public void set(int addr, int value) {
        activeMemory.set(addr, value);
    }

    public int get(int addr) {
        return activeMemory.get(addr);
    }

    public void setModel(final Model model) {
        switch (model) {
            case _48K:
                activeMemory = new Memory48K(clock);
                break;

            case PLUS_2:
                activeMemory = new MemoryPlus2(clock);
                break;
        }
    }

    public int[] getDisplayMemory() {
        return activeMemory.getDisplayMemory();
    }

    public boolean screenChanged() {
        return activeMemory.screenChanged();
    }

    public void markScreenDrawn() {
        activeMemory.markScreenDrawn();
    }

    public void copyIntoPage(final int[] source, final int destination) {
        activeMemory.copyIntoPage(source, destination);
    }

    @Override
    public boolean recognises(int low, int high) {
        return activeMemory.recognises(low, high);
    }

    @Override
    public int read(int low, int high) {
        return activeMemory.read(low, high);
    }

    @Override
    public void write(int low, int high, int value) {
        activeMemory.write(low, high, value);
    }
}
