package com.socialthingy.plusf.tape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundBlockBits implements BlockBits {
    private final List<TapeBlock> blocks = new ArrayList<>();
    private final SignalState signalState;
    private BlockBits currentBlock;
    private int blockIdx = 0;

    public CompoundBlockBits(final SignalState signalState, final TapeBlock ... blocks) {
        this.blocks.addAll(Arrays.asList(blocks));
        this.signalState = signalState;
        this.currentBlock = blocks[0].getBitList(signalState);
    }

    public CompoundBlockBits(final SignalState signalState, final List<TapeBlock> blocks) {
        this.blocks.addAll(blocks);
        this.signalState = signalState;
        this.currentBlock = blocks.get(0).getBitList(signalState);
    }

    @Override
    public boolean hasNext() {
        if (currentBlock != null) {
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
        final Boolean nextBit = currentBlock.next();
        if (!currentBlock.hasNext()) {
            currentBlock = nextBlock();
        }

        return nextBit;
    }

    public int skip(final int amount) {
        int remaining = amount;
        int skipped = 0;
        while (remaining > 0 && currentBlock != null) {
            final int skippedInBlock = currentBlock.skip(remaining);
            remaining -= skippedInBlock;
            skipped += skippedInBlock;
            if (!currentBlock.hasNext()) {
                currentBlock = nextBlock();
            }
        }

        return skipped;
    }

    private BlockBits nextBlock() {
        if (blocks == null) {
            return null;
        }

        blockIdx++;
        if (blockIdx >= blocks.size() || blockIdx < 0) {
            return null;
        }

        final TapeBlock nextBlock = blocks.get(blockIdx);
        return nextBlock.getBitList(signalState);
    }
}
