package com.socialthingy.qaopm.z80;

public interface Register {
    int set(int value);
    int get();
}

class BytePairRegister implements Register {

    private final ByteRegister lowReg;
    private final ByteRegister highReg;

    BytePairRegister(final ByteRegister highReg, final ByteRegister lowReg) {
        this.highReg = highReg;
        this.lowReg = lowReg;
    }

    @Override
    public int set(final int value) {
        int wordValue = value & 0xffff;
        this.highReg.set((wordValue & 0xff00) >> 8);
        this.lowReg.set(wordValue & 0xff);
        return wordValue;
    }

    @Override
    public int get() {
        return (this.highReg.get() << 8) | this.lowReg.get();
    }
}

class WordRegister implements Register {
    private int value;

    @Override
    public int set(int value) {
        return this.value = value & 0xffff;
    }

    @Override
    public int get() {
        return this.value;
    }

    public int getAndInc() {
        int value = this.value;
        set(value + 1);
        return value;
    }
}