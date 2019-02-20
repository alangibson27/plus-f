package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;

public class TapePlayer implements Iterator<Boolean> {
    private final Logger log = LoggerFactory.getLogger(TapePlayer.class);
    private final ButtonModel tapePresentModel;
    private final ButtonModel playButtonModel;
    private final ButtonModel stopButtonModel;
    private final ButtonModel rewindToStartButtonModel;
    private final ButtonModel jumpButtonModel;
    private int blockIdx = 0;
    private final SignalState signalState = new SignalState(false);
    private BlockSignal currentBlock = null;
    private int loopStart = -1;
    private int loopCount = 0;
    private Tape tape;

    private Set<TapeListener> tapeListeners = new HashSet<>();

    public TapePlayer() {
        this.tapePresentModel = new DefaultButtonModel();
        tapePresentModel.setEnabled(false);
        this.playButtonModel = new JToggleButton.ToggleButtonModel();
        this.stopButtonModel = new DefaultButtonModel();
        this.rewindToStartButtonModel = new DefaultButtonModel();
        this.jumpButtonModel = new DefaultButtonModel();

        rewindToStartButtonModel.addActionListener(action -> {
            rewindToStart();
        });
    }

    public void addTapeListener(final TapeListener tl) {
        tapeListeners.add(tl);
    }

    public void removeTapeListener(final TapeListener tl) {
        tapeListeners.remove(tl);
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

    public ButtonModel getJumpButtonModel() {
        return jumpButtonModel;
    }

    public void ejectTape() {
        ejectTape(true);
    }

    private void ejectTape(final boolean notifyListeners) {
        currentBlock = null;
        blockIdx = -1;
        loopStart = -1;
        tape = null;
        tapePresentModel.setEnabled(false);
        jumpButtonModel.setEnabled(false);
        playButtonModel.setSelected(false);
        playButtonModel.setEnabled(false);
        stopButtonModel.setEnabled(false);
        rewindToStartButtonModel.setEnabled(false);

        if (notifyListeners) {
            tapeListeners.forEach(tl -> tl.tapeChanged(null));
        }
    }

    public void setTape(final Tape tape) {
        ejectTape(false);
        this.tape = tape;
        this.currentBlock = nextBlock();
        tapePresentModel.setEnabled(true);
        jumpButtonModel.setEnabled(true);
        playButtonModel.setEnabled(true);
        stopButtonModel.setEnabled(true);
        rewindToStartButtonModel.setEnabled(true);
        tapeListeners.forEach(tl -> tl.tapeChanged(tape));
    }

    public void play() {
        if (tape != null) {
            signalState.set(false);
        }
    }

    public void rewindToStart() {
        if (tape != null) {
            blockIdx = -1;
            currentBlock = nextBlock();
        }
    }

    public void jumpToBlock(final int idx) {
        if (tape != null && idx < tape.getBlocks().size()) {
            blockIdx = idx - 1;
            currentBlock = nextBlock();
        }
    }

    public boolean isPlaying() {
        return playButtonModel.isSelected();
    }

    public int getCurrentBlock() {
        return this.blockIdx;
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
            final BlockSignal block = currentBlock;
            remaining -= block.skip(remaining);
            if (!block.hasNext()) {
                currentBlock = nextBlock();
            }
        }

        return hasNext() ? next() : false;
    }

    private BlockSignal nextBlock() {
        if (tape == null) {
            return null;
        }

        blockIdx++;
        if (blockIdx >= tape.getBlocks().size() || blockIdx < 0) {
            return null;
        }

        tapeListeners.forEach(tl -> tl.blockChanged(blockIdx));

        final TapeBlock nextBlock = tape.getBlocks().get(blockIdx);
        if (nextBlock.shouldStopTape()) {
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

        return nextBlock.getBlockSignal(signalState);
    }
}
