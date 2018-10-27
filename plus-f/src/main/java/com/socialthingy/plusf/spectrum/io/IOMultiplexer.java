package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.z80.IO;

import java.util.Arrays;
import java.util.List;

public class IOMultiplexer implements IO {
    private final List<IO> devices;

    public IOMultiplexer(final IO ... devices) {
        this.devices = Arrays.asList(devices);
    }

    @Override
    public boolean recognises(int low, int high) {
        return true;
    }

    public int read(int low, int high) {
        int result = 0;
        for (IO device: devices) {
            if (device.recognises(low, high)) {
                result = device.read(low, high);
            }
        }
        return result;
    }

    public void write(int low, int high, int value) {
        for (IO device: devices) {
            if (device.recognises(low, high)) {
                device.write(low, high, value);
            }
        }
    }
}
