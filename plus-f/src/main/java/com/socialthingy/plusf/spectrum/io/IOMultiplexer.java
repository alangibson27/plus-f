package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.z80.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOMultiplexer implements IO {
    private final List<IO> devices;

    public IOMultiplexer(final IO ... devices) {
        this.devices = new ArrayList<>(Arrays.asList(devices));
    }

    @Override
    public boolean recognises(int low, int high) {
        return true;
    }

    public int read(int low, int high) {
        for (IO device: devices) {
            if (device.recognises(low, high)) {
                return device.read(low, high);
            }
        }

        return 0xff;
    }

    public void write(int low, int high, int value) {
        for (IO device: devices) {
            if (device.recognises(low, high)) {
                device.write(low, high, value);
                return;
            }
        }
    }
}
