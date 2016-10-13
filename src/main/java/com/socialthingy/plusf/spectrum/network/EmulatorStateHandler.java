package com.socialthingy.plusf.spectrum.network;

import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.socialthingy.plusf.p2p.Deserialiser;
import com.socialthingy.plusf.p2p.Serialiser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class EmulatorStateHandler implements Serialiser, Deserialiser {
    @Override
    public Object deserialise(final ByteString in) {
        final ByteBuffer buf = in.asByteBuffer();
        buf.order(BIG_ENDIAN);

        final int[] memory = new int[0x10000];
        for (int i = 0x4000; i < 0x5b00; i++) {
            memory[i] = buf.get() & 0xff;
        }
        final boolean isFlashActive = buf.get() != 0;
        final List<Long> borderChanges = new ArrayList<>();
        while (buf.hasRemaining()) {
            borderChanges.add(buf.getLong());
        }
        return new EmulatorState(memory, borderChanges, isFlashActive);
    }

    @Override
    public void serialise(final Object obj, final ByteStringBuilder out) {
        final EmulatorState state = (EmulatorState) obj;
        final int[] memory = state.getMemory();
        for (int i = 0x4000; i < 0x5b00; i++) {
            out.putByte((byte) memory[i]);
        }

        out.putByte(state.isFlashActive() ? (byte) 1 : 0);

        for (Long borderChange: state.getBorderChanges()) {
            out.putLong(borderChange, BIG_ENDIAN);
        }
    }
}
