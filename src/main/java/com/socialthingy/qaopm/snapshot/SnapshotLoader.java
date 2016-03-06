package com.socialthingy.qaopm.snapshot;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.Processor;

import java.io.IOException;
import java.io.InputStream;

public class SnapshotLoader {
    private InputStream inputStream;

    public SnapshotLoader(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void read(final Processor processor, final int[] memory) throws IOException {
        processor.register("a").set(inputStream.read());
        processor.register("f").set(inputStream.read());
        processor.register("c").set(inputStream.read());
        processor.register("b").set(inputStream.read());
        processor.register("l").set(inputStream.read());
        processor.register("h").set(inputStream.read());
        processor.register("pc").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("sp").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("i").set(inputStream.read());

        final int rLowBits = inputStream.read() & 0b01111111;
        final int indicator = inputStream.read();
        final int rHighBit = (indicator & 0b1) << 7;
        processor.register("r").set(rHighBit | rLowBits);
        final int borderColour = (indicator & 0b1110) >> 1;
        final boolean memoryIsCompressed = (indicator & 0b00100000) != 0;

        processor.register("e").set(inputStream.read());
        processor.register("d").set(inputStream.read());

        processor.register("bc'").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("de'").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("hl'").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("af'").set(Word.from(inputStream.read(), inputStream.read()));

        processor.register("iy").set(Word.from(inputStream.read(), inputStream.read()));
        processor.register("ix").set(Word.from(inputStream.read(), inputStream.read()));

        processor.setIff(0, inputStream.read() > 0);
        processor.setIff(1, inputStream.read() > 0);

        final int indicator2 = inputStream.read();
        processor.setInterruptMode(indicator2 & 0b11);

        if (memoryIsCompressed) {
            loadMemoryFromCompressedBinary(0x4000, memory);
        } else {
            loadMemoryFromBinary(0x4000, memory);
        }

        final int[] endMarker = new int[]{
            inputStream.read(), inputStream.read(), inputStream.read(), inputStream.read()
        };

        if (endMarker[0] != 0x00 || endMarker[1] != 0xed || endMarker[2] != 0xed || endMarker[3] != 0x00) {
            throw new IOException(".z80 file format invalid");
        }
    }

    private void loadMemoryFromCompressedBinary(int base, final int[] memory) throws IOException {
        while (base < 0x10000) {
            final int nextByte = inputStream.read();
            if (nextByte == 0xed) {
                final int nextByte2 = inputStream.read();
                if (nextByte2 == 0xed) {
                    final int repetitions = inputStream.read();
                    final int value = inputStream.read();

                    for (int i = 0; i < repetitions; i++) {
                        memory[base++] = value;
                    }
                } else{
                    memory[base++] = nextByte;
                    memory[base++] = nextByte2;
                }
            } else {
                memory[base++] = nextByte;
            }
        }
    }

    private void loadMemoryFromBinary(final int base, final int[] memory) {
    }
}
