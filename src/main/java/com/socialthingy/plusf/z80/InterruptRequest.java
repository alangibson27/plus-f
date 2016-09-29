package com.socialthingy.plusf.z80;

public class InterruptRequest {
    private final InterruptingDevice interruptingDevice;

    public InterruptRequest(final InterruptingDevice interruptingDevice) {
        this.interruptingDevice = interruptingDevice;
    }

    public InterruptingDevice getDevice() {
        return interruptingDevice;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof InterruptRequest) {
            final InterruptRequest that = (InterruptRequest) o;
            return this.getDevice() == that.getDevice();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return interruptingDevice != null ? interruptingDevice.hashCode() : 0;
    }
}
