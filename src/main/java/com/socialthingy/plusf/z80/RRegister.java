package com.socialthingy.plusf.z80;

public class RRegister extends ByteRegister {
    public RRegister() {
        super("r");
    }

    public void increment(final int amount) {
        value = ((value & 0b10000000) | ((value + amount) & 0b01111111));
    }
}
