package com.socialthingy.qaopm.z80;

public class InterruptRequest {
    private final InterruptingDevice interruptingDevice;
    private final int mode;

    public InterruptRequest(final InterruptingDevice interruptingDevice, final int mode) {
        this.interruptingDevice = interruptingDevice;
        this.mode = mode;
    }
}
