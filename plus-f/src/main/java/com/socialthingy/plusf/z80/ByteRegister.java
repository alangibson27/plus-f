package com.socialthingy.plusf.z80;

public class ByteRegister implements Register {

    protected int value;
    private String name;

    ByteRegister(final String name) {
        this.name = name;
    }

    @Override
    public int set(int value) {
        return this.value = value & 0xff;
    }

    @Override
    public int get() {
        return this.value;
    }

    @Override
    public String name() {
        return name;
    }

    public byte signedGet() {
        return (byte) get();
    }
}
