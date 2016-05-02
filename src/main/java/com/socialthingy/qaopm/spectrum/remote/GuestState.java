package com.socialthingy.qaopm.spectrum.remote;

import java.io.Serializable;

public class GuestState implements Serializable {
    public static final int ABSENT = -1;

    private int port;
    private int accumulator;
    private int value;

    public GuestState(final int port, final int accumulator, final int value) {
        this.port = port;
        this.accumulator = accumulator;
        this.value = value;
    }

    public int getPort() {
        return port;
    }

    public int getAccumulator() {
        return accumulator;
    }

    public int getValue() {
        return value;
    }
}
