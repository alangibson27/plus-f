package com.socialthingy.plusf.z80;

public class IndexRegister extends BytePairRegister {
    private final String name;

    public IndexRegister(final ByteRegister highReg, final ByteRegister lowReg) {
        super(highReg, lowReg);
        this.name = highReg.name().substring(0, 2);
    }

    public int withOffset(int offset) {
        return 0xffff & (get() + (byte) offset);
    }

    public String name() {
        return this.name;
    }
}
