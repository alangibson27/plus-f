package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Deserialiser;
import com.socialthingy.p2p.Serialiser;

import java.nio.ByteBuffer;

public class GuestStateHandler implements Serialiser, Deserialiser {
    @Override
    public Object deserialise(final ByteBuffer in) {
        return new GuestState(in.getInt(), in.getInt());
    }

    @Override
    public void serialise(final Object obj, final ByteBuffer out) {
        final GuestState state = (GuestState) obj;
        out.putInt(state.getJoystickState());
        out.putInt(state.getJoystickType());
    }
}
