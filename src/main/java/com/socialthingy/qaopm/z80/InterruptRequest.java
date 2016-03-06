package com.socialthingy.qaopm.z80;

public class InterruptRequest {
    private final InterruptingDevice interruptingDevice;

    public InterruptRequest(final InterruptingDevice interruptingDevice) {
        this.interruptingDevice = interruptingDevice;
    }

    public InterruptingDevice getDevice() {
        return interruptingDevice;
    }
}
