package com.socialthingy.plusf.z80;

public class ByteRegister implements Register {

    private int value;

    @Override
    public int set(int value) {
        return this.value = value & 0xff;
    }

    @Override
    public int get() {
        return this.value;
    }

    public byte signedGet() {
        return (byte) get();
    }
}
