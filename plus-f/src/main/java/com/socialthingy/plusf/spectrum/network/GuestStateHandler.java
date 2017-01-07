package com.socialthingy.plusf.spectrum.network;

import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.socialthingy.plusf.p2p.Deserialiser;
import com.socialthingy.plusf.p2p.Serialiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class GuestStateHandler implements Serialiser, Deserialiser {
    private final Logger log = LoggerFactory.getLogger(GuestStateHandler.class);

    @Override
    public Object deserialise(final ByteString in) {
        final ByteBuffer buf = in.asByteBuffer();
        buf.order(BIG_ENDIAN);
        final GuestState gs = new GuestState(buf.getInt(), buf.getInt());
        if (buf.hasRemaining()) {
            log.warn("{} bytes remaining after deserialisation", buf.remaining());
        }
        return gs;
    }

    @Override
    public void serialise(final Object obj, final ByteStringBuilder out) {
        final GuestState state = (GuestState) obj;
        out.putInt(state.getEventType(), BIG_ENDIAN);
        out.putInt(state.getEventValue(), BIG_ENDIAN);
    }
}
