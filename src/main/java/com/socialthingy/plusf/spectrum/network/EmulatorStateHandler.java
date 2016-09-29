package com.socialthingy.plusf.spectrum.network;

import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.socialthingy.plusf.p2p.Deserialiser;
import com.socialthingy.plusf.p2p.Serialiser;

import java.nio.ByteBuffer;

import static com.socialthingy.plusf.spectrum.network.EmulatorState.BORDER_LINE_COUNT;
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

        final int[] borderLines = new int[BORDER_LINE_COUNT];
        for (int i = 0; i < BORDER_LINE_COUNT; i++) {
            borderLines[i] = buf.getInt();
        }

        return new EmulatorState(memory, borderLines, buf.get() != 0);
    }

    @Override
    public void serialise(final Object obj, final ByteStringBuilder out) {
        final EmulatorState state = (EmulatorState) obj;
        final int[] memory = state.getMemory();
        for (int i = 0x4000; i < 0x5b00; i++) {
            out.putByte((byte) memory[i]);
        }

        final int[] borderLines = state.getBorderLines();
        for (int i = 0; i < BORDER_LINE_COUNT; i++) {
            out.putInt(borderLines[i], BIG_ENDIAN);
        }

        out.putByte(state.isFlashActive() ? (byte) 1 : 0);
    }
}
