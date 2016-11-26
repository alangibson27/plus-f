package com.socialthingy.plusf.z80;

public class BytePairRegister implements Register {

    private final ByteRegister lowReg;
    private final ByteRegister highReg;
    private final String name;

    BytePairRegister(final ByteRegister highReg, final ByteRegister lowReg) {
        this.highReg = highReg;
        this.lowReg = lowReg;
        this.name = highReg.name() + lowReg.name();
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

    @Override
    public String name() {
        return name;
    }

    public int getLow() {
        return this.lowReg.get();
    }

    public int getHigh() {
        return this.highReg.get();
    }

    public void setLow(final int low) {
        this.lowReg.set(low);
    }

    public void setHigh(final int high) {
        this.highReg.set(high);
    }

    public Register highReg() {
        return this.highReg;
    }

    public Register lowReg() {
        return this.lowReg;
    }
}
