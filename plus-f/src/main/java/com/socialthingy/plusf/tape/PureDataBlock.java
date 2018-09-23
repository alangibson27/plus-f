package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PureDataBlock extends TapeBlock {
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

    private final Duration pauseLength;
    private final int[] data;
    private final int lowPulseLength;
    private final int highPulseLength;
    private final int finalByteBitsUsed;
    private final List<BlockSignalProvider> subBlocks = new ArrayList<>();

    public PureDataBlock(
        final Duration pauseLength,
        final int[] data,
        final int lowPulseLength,
        final int highPulseLength,
        final int finalByteBitsUsed
    ) {
        this.pauseLength = pauseLength;
        this.data = data;
        this.lowPulseLength = lowPulseLength;
        this.highPulseLength = highPulseLength;
        this.finalByteBitsUsed = finalByteBitsUsed;
        createSubBlocks();
    }

    public int[] getData() {
        return data;
    }

    @Override
    public BlockSignal getBlockSignal(SignalState signalState) {
        return new CompoundBlockSignal(signalState, subBlocks);
    }

    @Override
    public boolean isDataBlock() {
        return true;
    }

    private void createSubBlocks() {
        subBlocks.add(PureDataBlockSignal::new);

        if (!pauseLength.isZero()) {
            subBlocks.add(new PulseSequenceBlock(Adjustment.NO_CHANGE, MILLISECOND_PULSE));
            subBlocks.add(new PauseBlock(pauseLength));
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Pure data block: %d bytes, hi: %d, lo: %d, fbb: %d, pause: %d ms",
                data.length,
                highPulseLength,
                lowPulseLength,
                finalByteBitsUsed,
                pauseLength.toMillis()
        );
    }

    private class PureDataBlockSignal implements BlockSignal {
        private final int[] highPulses = new int[] {highPulseLength, highPulseLength};
        private final int[] lowPulses = new int[] {lowPulseLength, lowPulseLength};

        private int currentByte = 0;
        private int currentBit = 0;
        private PulseSequenceSignal currentBitSignal;

        public PureDataBlockSignal(final SignalState signalState) {
            if (data.length > 0) {
                currentBitSignal = new PulseSequenceSignal(
                    signalState,
                    Adjustment.NO_CHANGE,
                    bitPulses(data[0], 7)
                );
            }
        }

        @Override
        public int skip(final int amount) {
            int remaining = amount;
            int skipped = 0;
            while (remaining > 0 && currentBitSignal != null) {
                final int skippedInBlock = currentBitSignal.skip(remaining);
                skipped += skippedInBlock;
                remaining -= skippedInBlock;
                if (!currentBitSignal.hasNext()) {
                    nextBit();
                }
            }
            return skipped;
        }

        @Override
        public boolean hasNext() {
            return (currentBitSignal != null && currentBitSignal.hasNext()) || !endOfFinalByte();
        }

        @Override
        public Boolean next() {
            if (currentBitSignal.hasNext()) {
                return currentBitSignal.next();
            }

            nextBit();
            return currentBitSignal.next();
        }

        private void nextBit() {
            if (endOfFinalByte()) {
                currentBitSignal = null;
            } else {
                if (endOfNonFinalByte()) {
                    currentBit = 0;
                    currentByte++;
                } else {
                    currentBit++;
                }

                currentBitSignal.setPulseLengths(bitPulses(data[currentByte], 7 - currentBit));
            }
        }

        private boolean endOfFinalByte() {
            return currentByte == data.length - 1 && currentBit == finalByteBitsUsed - 1;
        }

        private boolean endOfNonFinalByte() {
            return currentByte != data.length - 1 && currentBit == 7;
        }

        private int[] bitPulses(final int dataByte, final int bit) {
            return (dataByte & (1 << bit)) == 0 ? lowPulses : highPulses;
        }
    }
}