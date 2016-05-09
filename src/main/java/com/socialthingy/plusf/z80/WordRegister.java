package com.socialthingy.plusf.z80;

public class WordRegister implements Register {
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

    public int decAndGet() {
        return set(value - 1);
    }

    public int getLow() {
        return get() & 0xff;
    }

    public int getHigh() {
        return get() >> 8;
    }
}
