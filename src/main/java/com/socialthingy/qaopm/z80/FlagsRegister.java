package com.socialthingy.qaopm.z80;

public class FlagsRegister extends ByteRegister {

    private int cFlagMask = 0b00000001;
    private int nFlagMask = 0b00000010;
    private int pFlagMask = 0b00000100;
    private int hFlagMask = 0b00010000;
    private int zFlagMask = 0b01000000;
    private int sFlagMask = 0b10000000;

    private int value;

    @Override
    public int set(int value) {
        return this.value = value;
    }

    @Override
    public int get() {
        return this.value;
    }

    public void setC(final boolean flagValue) {
        if (flagValue) {
            value |= cFlagMask;
        } else {
            value &= (0xff ^ cFlagMask);
        }
    }

    public boolean getC() {
        return (value & cFlagMask) > 0;
    }

    public void setN(final boolean flagValue) {
        if (flagValue) {
            value |= nFlagMask;
        } else {
            value &= (0xff ^ nFlagMask);
        }
    }

    public boolean getN() {
        return (value & nFlagMask) > 0;
    }

    public void setP(final boolean flagValue) {
        if (flagValue) {
            value |= pFlagMask;
        } else {
            value &= (0xff ^ pFlagMask);
        }
    }

    public boolean getP() {
        return (value & pFlagMask) > 0;
    }

    public void setH(final boolean flagValue) {
        if (flagValue) {
            value |= hFlagMask;
        } else {
            value &= (0xff ^ hFlagMask);
        }
    }

    public boolean getH() {
        return (value & hFlagMask) > 0;
    }

    public void setZ(final boolean flagValue) {
        if (flagValue) {
            value |= zFlagMask;
        } else {
            value &= (0xff ^ zFlagMask);
        }
    }

    public boolean getZ() {
        return (value & zFlagMask) > 0;
    }

    public void setS(final boolean flagValue) {
        if (flagValue) {
            value |= sFlagMask;
        } else {
            value &= (0xff ^ sFlagMask);
        }
    }

    public boolean getS() {
        return (value & sFlagMask) > 0;
    }
}
