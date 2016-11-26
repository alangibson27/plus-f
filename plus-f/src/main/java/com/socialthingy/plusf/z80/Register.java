package com.socialthingy.plusf.z80;

public interface Register {
    int set(int value);
    int get();
    String name();
}
