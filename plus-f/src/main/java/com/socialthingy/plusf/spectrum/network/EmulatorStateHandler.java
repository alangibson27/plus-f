package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Deserialiser;
import com.socialthingy.p2p.Serialiser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EmulatorStateHandler implements Serialiser, Deserialiser {
    private final  int[] memory = new int[0x10000];

    @Override
    public Object deserialise(final ByteBuffer in) {
        final int memoryBase = in.getInt();
        final int memoryLength = in.getInt();
        for (int i = 0; i < memoryLength; i++) {
            memory[memoryBase + i] = in.get() & 0xff;
        }
        final boolean isFlashActive = in.get() != 0;
        final List<Long> borderChanges = new ArrayList<>();
        while (in.remaining() >= 8) {
            borderChanges.add(in.getLong());
        }
        return new EmulatorState(memory, 0x4000, 0x1b00, borderChanges, isFlashActive);
    }

    @Override
    public void serialise(final Object obj, final ByteBuffer out) {
        final EmulatorState state = (EmulatorState) obj;
        out.putInt(state.getMemoryBase());
        out.putInt(state.getMemoryLength());
        final int[] memory = state.getMemory();
        for (int i = 0; i < state.getMemoryLength(); i++) {
            out.put((byte) memory[state.getMemoryBase() + i]);
        }

        out.put(state.isFlashActive() ? (byte) 1 : 0);
        state.getBorderChanges().forEach(out::putLong);
    }
}
