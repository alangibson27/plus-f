package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Deserialiser;
import com.socialthingy.p2p.Serialiser;
import com.socialthingy.plusf.spectrum.display.PixelMapper;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.socialthingy.plusf.spectrum.display.DisplayComponent.BOTTOM_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.DisplayComponent.TOP_BORDER_HEIGHT;

public class EmulatorStateHandler implements Serialiser, Deserialiser {
    private final int[] memory = new int[0x1b00];

    @Override
    public Object deserialise(final ByteBuffer in) {
        final int memoryBase = in.getInt();
        final int memoryLength = in.getInt();
        for (int i = 0; i < memoryLength; i++) {
            memory[memoryBase + i] = in.get() & 0xff;
        }
        final boolean isFlashActive = in.get() != 0;
        final int[] borderColours = new int[TOP_BORDER_HEIGHT + PixelMapper.SCREEN_HEIGHT + BOTTOM_BORDER_HEIGHT];
        int line = 0;
        while (in.remaining() >= 4) {
            borderColours[line++] = in.getInt();
        }
        return new EmulatorState(memory, 0x0000, 0x1b00, borderColours, isFlashActive);
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
        Arrays.stream(state.getBorderColours()).forEach(out::putInt);
    }
}
