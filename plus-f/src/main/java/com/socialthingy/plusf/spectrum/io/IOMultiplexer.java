package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;

public class IOMultiplexer implements IO {
    private final List<IO> devices;
    private final IntSupplier unusedPortReader;

    public IOMultiplexer(final Model currentModel, final IO ... devices) {
        this.devices = new ArrayList<>(Arrays.asList(devices));
        switch (currentModel) {
            case PLUS_2A:
                unusedPortReader = () -> 0xff;
                break;

            default:
                final Random random = new Random();
                unusedPortReader = () -> random.nextInt() & 0xff;
        }
    }

    public void addDevice(final IO device) {
        this.devices.add(device);
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

        return unusedPortReader.getAsInt();
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
