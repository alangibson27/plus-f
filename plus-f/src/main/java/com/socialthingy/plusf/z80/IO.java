package com.socialthingy.plusf.z80;

public interface IO {
    boolean recognises(int low, int high);
    int read(int low, int high);
    void write(int low, int high, int value);
}
