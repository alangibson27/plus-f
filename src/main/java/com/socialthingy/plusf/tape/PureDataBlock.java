package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.IteratorIterator;
import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Iterator;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class PureDataBlock extends TapeBlock {

    public static final int[] FINAL_OPPOSITE_EDGE_PULSE = new int[]{3500};

    public static Try<PureDataBlock> read(final InputStream tzxFile) {
        try {
            final int zeroPulseLength = nextWord(tzxFile);
            final int onePulseLength = nextWord(tzxFile);
            final int finalByteBitsUsed = nextByte(tzxFile);
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextTriple(tzxFile);

            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(
                    new PureDataBlock(pauseLength, data, zeroPulseLength, onePulseLength, finalByteBitsUsed)
            );
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private static final int[] finalByteMask = new int[]{
            binary("10000000"),
            binary("11000000"),
            binary("11100000"),
            binary("11110000"),
            binary("11111000"),
            binary("11111100"),
            binary("11111110"),
            binary("11111111")
    };

    private final Duration pauseLength;
    private final int[] data;
    private final int zeroPulseLength;
    private final int onePulseLength;
    private final int finalByteBitsUsed;

    public PureDataBlock(
        final Duration pauseLength,
        final int[] data,
        final int zeroPulseLength,
        final int onePulseLength,
        final int finalByteBitsUsed
    ) {
        this.pauseLength = pauseLength;
        this.data = data;
        this.zeroPulseLength = zeroPulseLength;
        this.onePulseLength = onePulseLength;
        this.finalByteBitsUsed = finalByteBitsUsed;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public Iterator<Bit> bits(final SignalState signalState) {
        final PureDataIterator dataIterator = new PureDataIterator(signalState);
        if (pauseLength.isZero()) {
            return dataIterator;
        } else {
            return new IteratorIterator<>(
                dataIterator,
                new PulseSequenceIterator(Adjustment.NO_CHANGE, signalState, FINAL_OPPOSITE_EDGE_PULSE),
                new PauseIterator(signalState, pauseLength)
            );
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Pure data block: %d bytes, hi: %d, lo: %d, fbb: %d, pause: %d ms",
                data.length,
                onePulseLength,
                zeroPulseLength,
                finalByteBitsUsed,
                pauseLength.toMillis()
        );
    }

    private class PureDataIterator implements Iterator<Bit> {
        private final SignalState signalState;
        private int byteIdx = 0;
        private DataByteIterator currentIterator;

        public PureDataIterator(final SignalState signalState) {
            this.signalState = signalState;
            this.currentIterator = new DataByteIterator(signalState, data[byteIdx], data.length > 1 ? 8 : finalByteBitsUsed);
        }

        @Override
        public boolean hasNext() {
            return currentIterator.hasNext();
        }

        @Override
        public Bit next() {
            final Bit nextValue = currentIterator.next();
            if (!currentIterator.hasNext()) {
                byteIdx++;
                if (byteIdx < data.length) {
                    if (byteIdx == data.length - 1) {
                        currentIterator = new DataByteIterator(signalState, data[byteIdx], finalByteBitsUsed);
                    } else {
                        currentIterator = new DataByteIterator(signalState, data[byteIdx], 8);
                    }
                }
            }
            return nextValue;
        }
    }

    private class DataByteIterator implements Iterator<Bit> {
        private final SignalState signalState;
        private final int dataByte;
        private PulseSequenceIterator bitIterator;
        private int bitIdx = 7;
        private final int lastBit;

        public DataByteIterator(final SignalState signalState, final int dataByte, final int bitsUsed) {
            this.signalState = signalState;
            this.dataByte = dataByte;
            this.bitIterator = new PulseSequenceIterator(Adjustment.NO_CHANGE, signalState, bitPulses(dataByte, bitIdx));
            this.lastBit = 8 - bitsUsed;
        }

        @Override
        public boolean hasNext() {
            return bitIterator.hasNext();
        }

        @Override
        public Bit next() {
            final Bit nextValue = bitIterator.next();
            if (!bitIterator.hasNext()) {
                bitIdx--;
                if (bitIdx >= lastBit) {
                    bitIterator.reset(bitPulses(dataByte, bitIdx));
                }
            }
            return nextValue;
        }
    }

    private int[] bitPulses(final int dataByte, final int bit) {
        final boolean high = (dataByte & (1 << bit)) != 0;
        if (high) {
            return new int[] {onePulseLength, onePulseLength};
        } else {
            return new int[] {zeroPulseLength, zeroPulseLength};
        }
    }
}