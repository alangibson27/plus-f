package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.replist.SkippableIterator;

import javax.swing.*;
import java.time.Duration;
import java.util.*;

public class TapePlayer implements Iterator<Boolean> {
    private final ButtonModel tapePresentModel;
    private final ButtonModel playButtonModel;
    private final ButtonModel stopButtonModel;
    private final ButtonModel rewindToStartButtonModel;
    private int blockIdx = 0;
    private final SignalState signalState = new SignalState(false);
    private SkippableIterator<Boolean> currentBlock = null;
    private int loopStart = -1;
    private int loopCount = 0;
    private Tape tape;

    private TapeBlock[] blocks = null;

    public TapePlayer() {
        this.tapePresentModel = new DefaultButtonModel();
        tapePresentModel.setEnabled(false);
        this.playButtonModel = new JToggleButton.ToggleButtonModel();
        this.stopButtonModel = new DefaultButtonModel();
        this.rewindToStartButtonModel = new DefaultButtonModel();

        rewindToStartButtonModel.addActionListener(action -> {
            try {
                rewindToStart();
            } catch (TapeException e) {
                e.printStackTrace();
            }
        });
    }

    public Optional<Tape> getTape() {
        return Optional.ofNullable(tape);
    }

    public ButtonModel getPlayButtonModel() {
        return playButtonModel;
    }

    public ButtonModel getStopButtonModel() {
        return stopButtonModel;
    }

    public ButtonModel getRewindToStartButtonModel() {
        return rewindToStartButtonModel;
    }

    public ButtonModel getTapePresentModel() {
        return tapePresentModel;
    }

    public void ejectTape() {
        currentBlock = null;
        blocks = null;
        blockIdx = -1;
        loopStart = -1;
        tape = null;
        tapePresentModel.setEnabled(false);
        playButtonModel.setSelected(false);
        playButtonModel.setEnabled(false);
        stopButtonModel.setEnabled(false);
        rewindToStartButtonModel.setEnabled(false);
    }

    public void setTape(final Tape tape) throws TapeException {
        ejectTape();
        this.tape = tape;
        final TapeBlock[] blocks = new TapeBlock[tape.getBlocks().length + 2];
        System.arraycopy(tape.getBlocks(), 0, blocks, 0, tape.getBlocks().length);
        blocks[blocks.length - 2] = new PulseSequenceBlock(Adjustment.NO_CHANGE, new int[] {3500});
        blocks[blocks.length - 1] = new PauseBlock(Duration.ofMillis(1));
        this.blocks = blocks;
        this.currentBlock = nextBlock();
        tapePresentModel.setEnabled(true);
        playButtonModel.setEnabled(true);
        stopButtonModel.setEnabled(true);
        rewindToStartButtonModel.setEnabled(true);
    }

    public void play() {
        if (blocks != null) {
            signalState.set(false);
        }
    }

    public void rewindToStart() throws TapeException {
        if (blocks != null) {
            blockIdx = -1;
            currentBlock = nextBlock();
        }
    }

    public boolean isPlaying() {
        return playButtonModel.isSelected();
    }

    @Override
    public boolean hasNext() {
        if (!isPlaying()) {
            return false;
        } else if (currentBlock != null) {
            if (currentBlock.hasNext()) {
                return true;
            } else {
                currentBlock = nextBlock();
                return hasNext();
            }
        } else {
            return false;
        }
    }

    @Override
    public Boolean next() {
        if (!isPlaying() || currentBlock == null) {
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
            playButtonModel.setSelected(false);
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
