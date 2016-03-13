package com.socialthingy.qaopm.z80;

public class IndexRegister extends BytePairRegister {
    public IndexRegister(final ByteRegister highReg, final ByteRegister lowReg) {
        super(highReg, lowReg);
    }

    public int withOffset(int offset) {
        return 0xffff & (get() + (byte) offset);
    }
}
