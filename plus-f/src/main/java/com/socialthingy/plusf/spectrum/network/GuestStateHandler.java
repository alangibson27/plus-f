package com.socialthingy.plusf.spectrum.network;

import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.socialthingy.plusf.p2p.Deserialiser;
import com.socialthingy.plusf.p2p.Serialiser;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class GuestStateHandler implements Serialiser, Deserialiser {
    @Override
    public Object deserialise(final ByteString in) {
        final ByteBuffer buf = in.asByteBuffer();
        buf.order(BIG_ENDIAN);
        return new GuestState(buf.getInt(), buf.getInt());
    }

    @Override
    public void serialise(final Object obj, final ByteStringBuilder out) {
        final GuestState state = (GuestState) obj;
        out.putInt(state.getEventType(), BIG_ENDIAN);
        out.putInt(state.getEventValue(), BIG_ENDIAN);
    }
}
