package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.tape.SignalState.Adjustment;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.Duration;
import java.util.*;

public class TapePlayer implements Iterator<TapeBlock.Bit> {
    private int blockIdx = 0;
    private final SignalState signalState = new SignalState(false);
    private Optional<Iterator<TapeBlock.Bit>> currentBlock;
    private final Stack<Integer> loopStack = new Stack<>();

    private Optional<TapeBlock[]> blocks = Optional.empty();
    private SimpleBooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty playAvailable = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty stopAvailable = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty seekAvailable = new SimpleBooleanProperty(false);

    public SimpleBooleanProperty playAvailableProperty() {
        return playAvailable;
    }

    public SimpleBooleanProperty stopAvailableProperty() {
        return stopAvailable;
    }

    public SimpleBooleanProperty seekAvailableProperty() {
        return seekAvailable;
    }

    public SimpleBooleanProperty playingProperty() {
        return isPlaying;
    }

    public void ejectTape() {
        blocks = Optional.empty();
        blockIdx = -1;
        loopStack.clear();
        playAvailable.set(false);
        stopAvailable.set(false);
        seekAvailable.set(false);
    }

    public void setTape(final Tape tape) throws TapeException {
        ejectTape();
        final TapeBlock[] blocks = new TapeBlock[tape.getBlocks().length + 2];
        System.arraycopy(tape.getBlocks(), 0, blocks, 0, tape.getBlocks().length);
        blocks[blocks.length - 2] = new PulseSequenceBlock(Adjustment.NO_CHANGE, new int[] {3500});
        blocks[blocks.length - 1] = new PauseBlock(Duration.ofMillis(1));
        this.blocks = Optional.of(blocks);
        this.currentBlock = nextBlock();
        playAvailable.set(true);
        stopAvailable.set(false);
        seekAvailable.set(false);
    }

    public void stop() {
        if (blocks.isPresent()) {
            isPlaying.set(false);
            playAvailable.set(true);
            stopAvailable.set(false);
            seekAvailable.set(true);
        }
    }

    public void play() {
        if (blocks.isPresent()) {
            signalState.set(false);
            isPlaying.set(true);
            playAvailable.set(false);
            stopAvailable.set(true);
            seekAvailable.set(true);
        }
    }

    public void rewindToStart() throws TapeException {
        if (blocks.isPresent()) {
            isPlaying.set(false);
            blockIdx = -1;
            currentBlock = nextBlock();
            playAvailable.set(true);
            stopAvailable.set(false);
            seekAvailable.set(true);
        }
    }

    @Override
    public boolean hasNext() {
        if (!isPlaying.get()) {
            return false;
        } else {
            final boolean hasNext = currentBlock.map(Iterator::hasNext).orElse(false);
            if (!hasNext) {
                stop();
            }
            return hasNext;
        }
    }

    @Override
    public TapeBlock.Bit next() {
        if (!isPlaying.get() || !currentBlock.isPresent()) {
            throw new NoSuchElementException("Tape stopped");
        } else {
            final Iterator<TapeBlock.Bit> block = currentBlock.get();
            final TapeBlock.Bit nextBit = block.next();
            if (nextBit instanceof StopTapeBit) {
                stop();
            }

            if (!block.hasNext()) {
                currentBlock = nextBlock();
            }

            return nextBit;
        }
    }

    private Optional<Iterator<TapeBlock.Bit>> nextBlock() {
        if (!blocks.isPresent()) {
            return Optional.empty();
        }

        blockIdx++;
        final TapeBlock[] blocks = this.blocks.get();
        if (blockIdx >= blocks.length) {
            return Optional.empty();
        }

        final TapeBlock nextBlock = blocks[blockIdx];
        if (nextBlock instanceof LoopStartBlock) {
            loopStack.push(blockIdx);
            return nextBlock();
        }

        if (nextBlock instanceof LoopEndBlock) {
            if (loopStack.isEmpty()) {
                return Optional.empty();
            } else {
                blockIdx = loopStack.pop();
                return nextBlock();
            }
        }

        return Optional.of(nextBlock.bits(signalState));
    }

}
