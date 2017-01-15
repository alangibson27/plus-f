package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Deserialiser;
import com.socialthingy.p2p.Serialiser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EmulatorStateHandler implements Serialiser, Deserialiser {
    @Override
    public Object deserialise(final ByteBuffer in) {
        final int[] memory = new int[0x10000];
        for (int i = 0x4000; i < 0x5b00; i++) {
            memory[i] = in.get() & 0xff;
        }
        final boolean isFlashActive = in.get() != 0;
        final List<Long> borderChanges = new ArrayList<>();
        while (in.remaining() >= 8) {
            borderChanges.add(in.getLong());
        }
        return new EmulatorState(memory, borderChanges, isFlashActive);
    }

    @Override
    public void serialise(final Object obj, final ByteBuffer out) {
        final EmulatorState state = (EmulatorState) obj;
        final int[] memory = state.getMemory();
        for (int i = 0x4000; i < 0x5b00; i++) {
            out.put((byte) memory[i]);
        }

        out.put(state.isFlashActive() ? (byte) 1 : 0);
        state.getBorderChanges().forEach(out::putLong);
    }
}
