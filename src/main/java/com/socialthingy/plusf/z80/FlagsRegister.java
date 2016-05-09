package com.socialthingy.plusf.z80;

public class FlagsRegister extends ByteRegister {

    public enum Flag {
        C(0b00000001),
        N(0b00000010),
        P(0b00000100),
        H(0b00010000),
        Z(0b01000000),
        S(0b10000000);

        private int mask;

        Flag(int mask) {
            this.mask = mask;
        }
    }

    private int value;

    @Override
    public int set(int value) {
        return this.value = value;
    }

    @Override
    public int get() {
        return this.value;
    }

    public void set(final Flag flag, final boolean flagValue) {
        if (flagValue) {
            value |= flag.mask;
        } else {
            value &= (0xff ^ flag.mask);
        }
    }

    public boolean get(final Flag flag) {
        return (value & flag.mask) > 0;
    }
}
