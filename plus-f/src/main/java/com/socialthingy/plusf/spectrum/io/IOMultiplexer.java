package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.z80.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOMultiplexer implements IO {
    private final List<IO> devices;
    private final SpectrumMemory memory;

    public IOMultiplexer(final SpectrumMemory memory, final IO ... devices) {
        this.devices = new ArrayList<>(Arrays.asList(devices));
        this.devices.add(memory);
        this.memory = memory;
    }

    @Override
    public boolean recognises(int low, int high) {
        return true;
    }

    public int read(int low, int high) {
        if (high >= 0x40 && high < 0x80) {
            handleIOContention();
        }

        for (IO device: devices) {
            if (device.recognises(low, high)) {
                return device.read(low, high);
            }
        }

        return 0xff;
    }

    public void write(int low, int high, int value) {
        if (high >= 0x40 && high < 0x80) {
            handleIOContention();
        }

        for (IO device: devices) {
            if (device.recognises(low, high)) {
                device.write(low, high, value);
                return;
            }
        }
    }

    private void handleIOContention() {
        memory.handleMemoryContention(1);
    }
}
