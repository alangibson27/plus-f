package com.socialthingy.plusf.z80;

public class Memory {
    protected final int[] addressableMemory;

    public Memory() {
        this.addressableMemory = new int[0x10000];
    }

    public void set(int addr, final int value) {
        addressableMemory[addr] = value;
    }

    public int get(final int addr) {
        return addressableMemory[addr & 0xffff];
    }

    protected void copyInto(final int[] src, final int destAddr) {
        System.arraycopy(src, 0, addressableMemory, destAddr, src.length);
    }

    protected void copyFrom(final int srcAddr, final int[] dest) {
        System.arraycopy(addressableMemory, srcAddr, dest, 0, dest.length);
    }
}
