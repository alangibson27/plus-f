package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.IteratorIterator;
import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Iterator;

public class VariableSpeedBlock extends TapeBlock {

    public static Try<VariableSpeedBlock> readTapBlock(final InputStream tzxFile, final int dataLength) {
        try {
            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(new VariableSpeedBlock(Duration.ofSeconds(1), data));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public static Try<VariableSpeedBlock> readStandardSpeedBlock(final InputStream tzxFile) {
        try {
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextWord(tzxFile);
            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(new VariableSpeedBlock(pauseLength, data));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public static Try<VariableSpeedBlock> readTurboSpeedBlock(final InputStream tzxFile) {
        try {
            final int pilotPulseLength = nextWord(tzxFile);
            final int sync1PulseLength = nextWord(tzxFile);
            final int sync2PulseLength = nextWord(tzxFile);
            final int zeroPulseLength = nextWord(tzxFile);
            final int onePulseLength = nextWord(tzxFile);
            final int pilotToneLength = nextWord(tzxFile);
            final int finalByteBitsUsed = nextByte(tzxFile);
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextTriple(tzxFile);

            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(
                new VariableSpeedBlock(pauseLength, data, pilotPulseLength, sync1PulseLength, sync2PulseLength, zeroPulseLength, onePulseLength, pilotToneLength, finalByteBitsUsed)
            );
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    protected final Duration pauseLength;
    protected final int[] data;
    protected final int pilotPulseLength;
    protected final int sync1PulseLength;
    protected final int sync2PulseLength;
    protected final int zeroPulseLength;
    protected final int onePulseLength;
    protected final int pilotToneLength;
    protected final int finalByteBitsUsed;
    private String description;

    public VariableSpeedBlock(
        final Duration pauseLength,
        final int[] data,
        final int pilotPulseLength,
        final int sync1PulseLength,
        final int sync2PulseLength,
        final int zeroPulseLength,
        final int onePulseLength,
        final int pilotToneLength,
        final int finalByteBitsUsed
    ) {
        this.pauseLength = pauseLength;
        this.data = data;
        this.pilotPulseLength = pilotPulseLength;
        this.sync1PulseLength = sync1PulseLength;
        this.sync2PulseLength = sync2PulseLength;
        this.zeroPulseLength = zeroPulseLength;
        this.onePulseLength = onePulseLength;
        this.pilotToneLength = pilotToneLength;
        this.finalByteBitsUsed = finalByteBitsUsed;
        this.description = "turbo";
    }

    public VariableSpeedBlock(final Duration pauseLength, final int[] data) {
        this(pauseLength, data, 2168, 667, 735, 855, 1710, data[0] < 128 ? 8063 : 3223, 8);
        this.description = "standard";
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public Iterator<Boolean> bits(SignalState signalState) {
        final PureToneBlock pilot = new PureToneBlock(pilotPulseLength, pilotToneLength);
        final PulseSequenceBlock pulseSequence = new PulseSequenceBlock(Adjustment.NO_CHANGE, new int[] {sync1PulseLength, sync2PulseLength});
        final PureDataBlock pureData = new PureDataBlock(pauseLength, data, zeroPulseLength, onePulseLength, finalByteBitsUsed);
        return new IteratorIterator<>(
            pilot.bits(signalState),
            pulseSequence.bits(signalState),
            pureData.bits(signalState)
        );
    }

    @Override
    public String toString() {
        return String.format("%s speed block", description);
    }
}
