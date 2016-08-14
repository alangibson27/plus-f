package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.z80.IO;

public class SettableIOPort implements IO {
    private final int port;
    private int value = 0;

    public SettableIOPort(final int port) {
        this.port = port;
    }

    public void setValue(final int value) {
        this.value = value;
    }

    @Override
    public int read(final int port, final int accumulator) {
        return port == this.port ? value : 0;
    }

    @Override
    public void write(final int port, final int accumulator, final int value) {
    }
}
