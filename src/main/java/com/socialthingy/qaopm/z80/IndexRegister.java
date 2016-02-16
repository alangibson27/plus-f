package com.socialthingy.qaopm.z80;

public class IndexRegister extends WordRegister {
    public int withOffset(int offset) {
        return 0xffff & (get() + (byte) offset);
    }
}
