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
        final int aValue = inputStream.read();
        final int fValue = inputStream.read();
        final int cValue = inputStream.read();
        final int bValue = inputStream.read();
        final int lValue = inputStream.read();
        final int hValue = inputStream.read();
        final int pcValue = Word.from(inputStream.read(), inputStream.read());
        final int spValue = Word.from(inputStream.read(), inputStream.read());
        final int iValue = inputStream.read();

        final int rLowBits = inputStream.read() & 0b01111111;
        final int indicator = inputStream.read();
        final int rHighBit = (indicator & 0b1) << 7;
        final int borderColour = (indicator & 0b1110) >> 1;
        final boolean memoryIsCompressed = (indicator & 0b00100000) != 0;

        final int eValue = inputStream.read();
        final int dValue = inputStream.read();

        final int bcPValue = Word.from(inputStream.read(), inputStream.read());
        final int dePValue = Word.from(inputStream.read(), inputStream.read());
        final int hlPValue = Word.from(inputStream.read(), inputStream.read());
        final int aPValue = inputStream.read();
        final int fPValue = inputStream.read();

        final int iyValue = Word.from(inputStream.read(), inputStream.read());
        final int ixValue = Word.from(inputStream.read(), inputStream.read());

        final int iff0 = inputStream.read();
        final int iff1 = inputStream.read();

        final int indicator2 = inputStream.read();

        final int[] newMemory = new int[0xc000];
        if (memoryIsCompressed) {
            loadMemoryFromCompressedBinary(0x0000, newMemory);
        } else {
            loadMemoryFromBinary(0x0000, newMemory);
        }

        final int[] endMarker = new int[]{
            inputStream.read(), inputStream.read(), inputStream.read(), inputStream.read()
        };

        if (endMarker[0] != 0x00 || endMarker[1] != 0xed || endMarker[2] != 0xed || endMarker[3] != 0x00) {
            throw new IOException(".z80 file format invalid");
        }

        processor.register("a").set(aValue);
        processor.register("f").set(fValue);
        processor.register("c").set(cValue);
        processor.register("b").set(bValue);
        processor.register("l").set(lValue);
        processor.register("h").set(hValue);
        processor.register("pc").set(pcValue);
        processor.register("sp").set(spValue);
        processor.register("i").set(iValue);
        processor.register("r").set(rHighBit | rLowBits);
        processor.register("e").set(eValue);
        processor.register("d").set(dValue);

        processor.register("bc'").set(bcPValue);
        processor.register("de'").set(dePValue);
        processor.register("hl'").set(hlPValue);
        processor.register("a'").set(aPValue);
        processor.register("f'").set(fPValue);

        processor.register("iy").set(iyValue);
        processor.register("ix").set(ixValue);

        processor.setIff(0, iff0 > 0);
        processor.setIff(1, iff1 > 0);

        processor.setInterruptMode(indicator2 & 0b11);

        System.arraycopy(newMemory, 0, memory, 0x4000, 0xc000);
    }

    private void loadMemoryFromCompressedBinary(int base, final int[] memory) throws IOException {
        while (base < 0xc000) {
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
