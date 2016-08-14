package com.socialthingy.plusf.spectrum.remote;

import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GuestState {
    private int eventType;
    private int eventValue;

    public GuestState(final int eventType, final int eventValue) {
        this.eventType = eventType;
        this.eventValue = eventValue;
    }

    public int getEventType() {
        return eventType;
    }

    public int getEventValue() {
        return eventValue;
    }

    public static void serialise(final Pair<GuestState, OutputStream> stateAndStream) {
        final GuestState state = stateAndStream.getKey();
        final OutputStream out = stateAndStream.getValue();

        try {
            out.write(state.eventType);
            out.write(state.eventValue);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static GuestState deserialise(final InputStream in) {
        try {
            final GuestState gs = new GuestState(in.read(), in.read());
            return gs;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
