package com.socialthingy.qaopm.spectrum.io;

import com.socialthingy.qaopm.z80.IO;

public class SinglePortIO implements IO {
    private final int port;
    private int value = 0;

    public SinglePortIO(final int port) {
        this.port = port;
    }

    public void setValue(final int value) {
        this.value = value;
    }

    @Override
    public int read(int port, int accumulator) {
        return port == this.port ? value : 0;
    }

    @Override
    public void write(int port, int accumulator, int value) {
    }
}
