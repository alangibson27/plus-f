package com.socialthingy.plusf.z80;

public class UncontendedMemory implements Memory {
    protected final int[] addressableMemory;
    protected final Clock clock;

    public UncontendedMemory(final Clock clock) {
        this.clock = clock;
        this.addressableMemory = new int[0x10000];
    }

    @Override
    public void set(final int addr, final int value) {
        clock.tick(3);
        addressableMemory[addr & 0xffff] = value;
    }

    @Override
    public int get(final int addr) {
        clock.tick(3);
        return addressableMemory[addr & 0xffff];
    }

    protected void copyInto(final int[] src, final int destAddr) {
        System.arraycopy(src, 0, addressableMemory, destAddr, src.length);
    }
}
