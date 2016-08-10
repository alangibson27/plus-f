package com.socialthingy.plusf.spectrum.remote;

import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GuestState {
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

    public static void serialise(final Pair<GuestState, OutputStream> stateAndStream) {
        final GuestState state = stateAndStream.getKey();
        final OutputStream out = stateAndStream.getValue();

        try {
            out.write(state.port);
            out.write(state.accumulator);
            out.write(state.value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static GuestState deserialise(final InputStream in) {
        try {
            return new GuestState(in.read(), in.read(), in.read());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
