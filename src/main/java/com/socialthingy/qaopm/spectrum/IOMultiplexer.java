package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.z80.IO;

public class IOMultiplexer implements IO {
    private IO[] devices = new IO[0x100];

    public void register(final int port, final IO device) {
        devices[port] = device;
    }

    @Override
    public int read(int port, int accumulator) {
        if (devices[port] != null) {
            return devices[port].read(port, accumulator);
        }
        return 0;
    }

    @Override
    public void write(int port, int accumulator, int value) {
        if (devices[port] != null) {
            devices[port].write(port, accumulator, value);
        }
    }
}
