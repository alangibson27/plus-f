package com.socialthingy.plusf.z80;

public interface Memory {
    void set(int addr, int value);
    int get(int addr);
}
