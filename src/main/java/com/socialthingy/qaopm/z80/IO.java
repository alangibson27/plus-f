package com.socialthingy.qaopm.z80;

public interface IO {
    int read(int port, int accumulator);
    void write(int port, int accumulator, int value);
}
