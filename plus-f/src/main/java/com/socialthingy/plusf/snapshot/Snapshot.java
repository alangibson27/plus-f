package com.socialthingy.plusf.snapshot;

import com.socialthingy.plusf.spectrum.Computer;
import com.socialthingy.plusf.spectrum.io.*;
import com.socialthingy.plusf.ui.JoystickKeys;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Snapshot {
    private InputStream inputStream;
    private int aValue;
    private int fValue;
    private int cValue;
    private int bValue;
    private int lValue;
    private int hValue;
    private int pcValue;
    private int spValue;
    private int iValue;
    private int rValue;
    private int eValue;
    private int dValue;
    private int bcPValue;
    private int dePValue;
    private int hlPValue;
    private int aPValue;
    private int fPValue;
    private int iyValue;
    private int ixValue;
    private int iff0;
    private int iff1;
    private int interruptMode;
    private int[][] memoryPages;
    private int borderColour;
    private Model model;
    private int lastWriteTo0x7ffd;
    private int lastWriteTo0xfffd;
    private int lastWriteTo0x1ffd;
    private int leftKey;
    private int rightKey;
    private int upKey;
    private int downKey;
    private int fireKey;

    private Map<Integer, Model> v2HardwareModes = Map.of(
        0, Model._48K,
        1, Model._48K,
        3, Model._128K,
        4, Model._128K
    );
    private Map<Integer, Model> v3HardwareModes = Map.of(
        0, Model._48K,
        1, Model._48K,
        3, Model._48K,
        4, Model._128K,
        5, Model._128K,
        6, Model._128K,
        12, Model.PLUS_2,
        13, Model.PLUS_2A
    );

    public Snapshot(final InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        memoryPages = new int[12][];
        for (int i = 0; i < 12; i++) {
            memoryPages[i] = new int[0x4000];
        }
        read();
    }

    public Snapshot(final Computer computer) {
        aValue = computer.getProcessor().register("a").get();
        fValue = computer.getProcessor().register("f").get();
        cValue = computer.getProcessor().register("c").get();
        bValue = computer.getProcessor().register("b").get();
        lValue = computer.getProcessor().register("l").get();
        hValue = computer.getProcessor().register("h").get();
        hValue = computer.getProcessor().register("pc").get();
    }

    private final int lsb(final int value) {
        return value & 0xff;
    }

    private final int msb(final int value) {
        return value >> 8;
    }

    public void write(final File target) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(target)) {
            fos.write(aValue);
            fos.write(fValue);
            fos.write(cValue);
            fos.write(bValue);
            fos.write(lValue);
            fos.write(hValue);
            fos.write(lsb(pcValue));
            fos.write(msb(pcValue));
            fos.write(lsb(spValue));
            fos.write(msb(spValue));
            fos.write(iValue);
            fos.write(rValue);
            fos.write(rValue);
            fos.write(rValue >> 7 | borderColour << 1 | 0x10);
            fos.write(eValue);
            fos.write(dValue);
            fos.write(lsb(bcPValue));
            fos.write(msb(bcPValue));
            fos.write(lsb(dePValue));
            fos.write(msb(dePValue));
            fos.write(lsb(hlPValue));
            fos.write(msb(hlPValue));
            fos.write(aPValue);
            fos.write(fPValue);
            fos.write(lsb(iyValue));
            fos.write(msb(iyValue));
            fos.write(lsb(ixValue));
            fos.write(msb(ixValue));
            fos.write(iff0);
            fos.write(iff1);
            fos.write(interruptMode);

            fos.write(0);
            fos.write(55);

            fos.write(lsb(pcValue));
            fos.write(msb(pcValue));
            fos.write(hwModeFrom(model));
            fos.write(model == Model._48K ? 0 : lastWriteTo0x7ffd);
            fos.write(0);
            fos.write(0);
            fos.write(lastWriteTo0xfffd);
            for (int i = 0; i < 16; i++) {
                fos.write(0);
            }
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(msb(leftKey));
            fos.write(lsb(leftKey));
            fos.write(msb(rightKey));
            fos.write(lsb(rightKey));
            fos.write(msb(downKey));
            fos.write(lsb(downKey));
            fos.write(msb(upKey));
            fos.write(lsb(upKey));
            fos.write(msb(fireKey));
            fos.write(lsb(fireKey));
            for (int i = 0; i < 10; i++) {
                fos.write(0);
            }
            fos.write(0);
            fos.write(0);
            fos.write(0);
            fos.write(lastWriteTo0x1ffd);

            if (model == Model._48K) {
                writeMemory(fos, 4);
                writeMemory(fos, 5);
                writeMemory(fos, 8);
            } else {
                for (int i = 1; i <= 8; i++) {
                    writeMemory(fos, i);
                }
            }
        }
    }

    private void writeMemory(final FileOutputStream fos, final int page) throws IOException {
        fos.write(0xff);
        fos.write(0xff);
        fos.write(page);
        for (int i = 0; i < 0xffff; i++) {
            fos.write(memoryPages[page][i]);
        }
    }

    public SpectrumMemory getMemory() {
        if (model == Model._48K) {
            final Memory48K memory = new Memory48K();
            memory.copyIntoPage(memoryPages[4], 2);
            memory.copyIntoPage(memoryPages[5], 3);
            memory.copyIntoPage(memoryPages[8], 1);

            return memory;
        } else {
            final Memory128K memory = new Memory128K(Model._128K);
            for (int i = 3; i <= 10; i++) {
                memory.copyIntoBank(memoryPages[i], i - 3);
            }

            memory.write(0xfd, 0x7f, lastWriteTo0x7ffd);
            memory.write(0xfd, 0xff, lastWriteTo0xfffd);
            memory.write(0xfd, 0x1f, lastWriteTo0x1ffd);
            return memory;
        }
    }

    public ContentionModel getContentionModel(final Clock clock, final SpectrumMemory memory) {
        switch (model) {
            case PLUS_2A:
                return new ContentionModelPlus2A(clock, memory);

            case PLUS_2:
            case _128K:
            case _128K_SPANISH:
                return new ContentionModel128K(clock, model, memory);

            default:
                return new ContentionModel48K(clock, model, memory);
        }
    }

    private void read() throws IOException {
        final SnapshotInfo snapshotInfo = extractCommonHeaders();
        borderColour = snapshotInfo.borderColour;
        if (pcValue == 0x0000) {
            final int headerLength = Word.from(inputStream.read(), inputStream.read());
            final int version = headerLength == 23 ? 2 : 3;
            pcValue = Word.from(inputStream.read(), inputStream.read());

            model = getHWMode(version, inputStream.read());
            if (model == null) {
                throw new IOException("Unsupported machine version. Only 48k and 128k spectrum snapshots supported.");
            }

            lastWriteTo0x7ffd = inputStream.read(); // byte 35
            skipByte(); // byte 36
            skipByte(); // byte 37
            lastWriteTo0xfffd = inputStream.read(); // byte 38
            skipBytes(16); // bytes 39-54

            if (headerLength > 23) {
                skipByte(); // byte 55
                skipByte(); // byte 56
                skipByte(); // byte 57
                skipByte(); // byte 58
                skipByte(); // byte 59
                skipByte(); // byte 60
                skipByte(); // byte 61
                skipByte(); // byte 62
                leftKey = readJoystickKey(); // bytes 63-64
                rightKey = readJoystickKey(); // bytes 65-66
                downKey = readJoystickKey(); // bytes 67-68
                upKey = readJoystickKey(); // bytes 69-70
                fireKey = readJoystickKey(); // bytes 71-72
                skipBytes(10); // bytes 73-82
                skipByte(); // byte 83
                skipByte(); // byte 84
                skipByte(); // byte 85
            }

            if (headerLength > 54) {
                lastWriteTo0x1ffd = inputStream.read(); // byte 86
            }

            int blockStart = inputStream.read();
            while (blockStart != -1) {
                final int blockLength = Word.from(blockStart, inputStream.read());
                final int pageNumber = inputStream.read();
                if (blockLength == 0xffff) {
                    loadMemoryFromBinary(memoryPages[pageNumber]);
                } else {
                    loadMemoryFromCompressedBinary(0, blockLength, memoryPages[pageNumber]);
                }

                blockStart = inputStream.read();
            }
        } else {
            model = Model._48K;
            extractV1Memory(snapshotInfo.memoryIsCompressed);
        }
    }

    private int readJoystickKey() throws IOException {
        final int row = inputStream.read();
        final int columnMask = inputStream.read();
        return KeyIdentifier.identify(row, columnMask);
    }

    private Model getHWMode(final int version, final int mode) {
        if (version == 2) {
            return v2HardwareModes.get(mode);
        } else if (version == 3) {
            return v3HardwareModes.get(mode);
        }

        return null;
    }

    private int hwModeFrom(final Model model) {
        final List<Integer> hwModes = v3HardwareModes.entrySet().stream().filter(e -> e.getValue() == model)
                .map(e -> e.getKey())
                .collect(Collectors.toList());

        return hwModes.isEmpty() ? 0 : hwModes.get(0);
    }

    private void extractV1Memory(final boolean memoryIsCompressed) throws IOException {
        final int[] wholeMemory = new int[0xc000];
        if (memoryIsCompressed) {
            loadMemoryFromCompressedBinary(0x0000, 0xc000, wholeMemory);
            final int[] endMarker = new int[]{
                    inputStream.read(), inputStream.read(), inputStream.read(), inputStream.read()
            };

            if (endMarker[0] != 0x00 || endMarker[1] != 0xed || endMarker[2] != 0xed || endMarker[3] != 0x00) {
                throw new IOException(".z80 file format invalid");
            }
        } else {
            loadMemoryFromBinary(wholeMemory);
        }

        System.arraycopy(wholeMemory, 0x4000, memoryPages[4], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x8000, memoryPages[5], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x0000, memoryPages[8], 0x0000, 0x4000);
    }

    private SnapshotInfo extractCommonHeaders() throws IOException {
        aValue = inputStream.read();
        fValue = inputStream.read();
        cValue = inputStream.read();
        bValue = inputStream.read();
        lValue = inputStream.read();
        hValue = inputStream.read();
        pcValue = Word.from(inputStream.read(), inputStream.read());
        spValue = Word.from(inputStream.read(), inputStream.read());
        iValue = inputStream.read();

        final int rLowBits = inputStream.read() & 0b01111111;
        final int indicator = inputStream.read();
        final int rHighBit = (indicator & 0b1) << 7;
        rValue = rHighBit | rLowBits;
        final int borderColour = (indicator & 0b1110) >> 1;
        final boolean memoryIsCompressed = (indicator & 0b00100000) != 0;

        eValue = inputStream.read();
        dValue = inputStream.read();

        bcPValue = Word.from(inputStream.read(), inputStream.read());
        dePValue = Word.from(inputStream.read(), inputStream.read());
        hlPValue = Word.from(inputStream.read(), inputStream.read());
        aPValue = inputStream.read();
        fPValue = inputStream.read();

        iyValue = Word.from(inputStream.read(), inputStream.read());
        ixValue = Word.from(inputStream.read(), inputStream.read());

        iff0 = inputStream.read();
        iff1 = inputStream.read();

        final int indicator2 = inputStream.read();
        interruptMode = indicator2 & 0b11;
        return new SnapshotInfo(memoryIsCompressed, borderColour);
    }

    public void setProcessorState(final Processor processor) {
        processor.register("a").set(aValue);
        processor.register("f").set(fValue);
        processor.register("c").set(cValue);
        processor.register("b").set(bValue);
        processor.register("l").set(lValue);
        processor.register("h").set(hValue);
        processor.register("pc").set(pcValue);
        processor.register("sp").set(spValue);
        processor.register("i").set(iValue);
        processor.register("r").set(rValue);
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

        processor.setInterruptMode(interruptMode);
    }

    public void setBorderColour(final ULA ula) {
        ula.setBorderColour(borderColour);
    }

    public Model getModel() {
        return model;
    }

    private void loadMemoryFromCompressedBinary(final int base, final int length, final int[] targetMemory) {
        final int[] decompressed = EDCompressor.INSTANCE.decompress(inputStream, length);
        System.arraycopy(decompressed, 0, targetMemory, base, decompressed.length);
    }

    private void loadMemoryFromBinary(final int[] memory) throws IOException {
        for (int i = 0; i < 0xc000; i++) {
            memory[i] = inputStream.read();
        }
    }

    private void skipByte() throws IOException {
        skipBytes(1);
    }

    private void skipBytes(final int bytes) throws IOException {
        inputStream.skip(bytes);
    }

    public JoystickKeys getJoystickKeys() {
        if (leftKey > 0 && rightKey > 0 && upKey > 0 && downKey > 0 && fireKey > 0) {
            return new JoystickKeys(upKey, downKey, leftKey, rightKey, fireKey);
        }
        return null;
    }

    private class SnapshotInfo {
        private boolean memoryIsCompressed;
        private int borderColour;

        public SnapshotInfo(boolean memoryIsCompressed, int borderColour) {
            this.memoryIsCompressed = memoryIsCompressed;
            this.borderColour = borderColour;
        }
    }
}
