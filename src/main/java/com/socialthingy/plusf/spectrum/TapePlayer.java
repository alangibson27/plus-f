package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.replist.SkippableIterator;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import org.controlsfx.control.Notifications;

import java.time.Duration;
import java.util.*;

public class TapePlayer implements Iterator<Boolean> {
    private int blockIdx = 0;
    private final SignalState signalState = new SignalState(false);
    private SkippableIterator<Boolean> currentBlock = null;
    private int loopStart = -1;
    private int loopCount = 0;

    private TapeBlock[] blocks = null;
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
        stop();
        currentBlock = null;
        blocks = null;
        blockIdx = -1;
        loopStart = -1;
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
        this.blocks = blocks;
        this.currentBlock = nextBlock();
        playAvailable.set(true);
        stopAvailable.set(false);
        seekAvailable.set(false);
    }

    public void stop() {
        if (blocks != null) {
            isPlaying.set(false);
            playAvailable.set(true);
            stopAvailable.set(false);
            seekAvailable.set(true);
        }
    }

    public void play() {
        if (blocks != null) {
            signalState.set(false);
            isPlaying.set(true);
            playAvailable.set(false);
            stopAvailable.set(true);
            seekAvailable.set(true);
        }
    }

    public void rewindToStart() throws TapeException {
        if (blocks != null) {
            Notifications.create().title("Tape").text("Tape has been rewound to start").showInformation();
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
        } else if (currentBlock != null) {
            if (currentBlock.hasNext()) {
                return true;
            } else {
                currentBlock = nextBlock();
                return hasNext();
            }
        } else {
            Platform.runLater(() -> Notifications.create().title("Tape").text("End of tape reached").showInformation());
            stop();
            return false;
        }
    }

    @Override
    public Boolean next() {
        if (!isPlaying.get() || currentBlock == null) {
            throw new NoSuchElementException("Tape stopped");
        } else {
            final Iterator<Boolean> block = currentBlock;
            final Boolean nextBit = block.next();
            if (!block.hasNext()) {
                currentBlock = nextBlock();
            }

            return nextBit;
        }
    }

    public boolean skip(final int amount) {
        int remaining = amount - 1;
        while (remaining > 0 && currentBlock != null) {
            final SkippableIterator<Boolean> block = currentBlock;
            remaining -= block.skip(remaining);
            if (!block.hasNext()) {
                currentBlock = nextBlock();
            }
        }

        return hasNext() ? next() : false;
    }

    private SkippableIterator<Boolean> nextBlock() {
        if (blocks == null) {
            return null;
        }

        blockIdx++;
        final TapeBlock[] blocks = this.blocks;
        if (blockIdx >= blocks.length || blockIdx < 0) {
            return null;
        }

        final TapeBlock nextBlock = blocks[blockIdx];
        if (nextBlock instanceof PauseBlock && ((PauseBlock) nextBlock).shouldStopTape()) {
            stop();
            return nextBlock();
        }

        if (nextBlock instanceof LoopStartBlock) {
            loopStart = blockIdx;
            loopCount = ((LoopStartBlock) nextBlock).getIterations();
            return nextBlock();
        }

        if (nextBlock instanceof LoopEndBlock) {
            loopCount--;
            if (loopCount > 0) {
                blockIdx = loopStart;
            }
            return nextBlock();
        }

        if (nextBlock instanceof JumpBlock) {
            blockIdx += (((JumpBlock) nextBlock).getJumpSize() - 1);
            return nextBlock();
        }

        return nextBlock.getBitList(signalState).iterator();
    }

}
