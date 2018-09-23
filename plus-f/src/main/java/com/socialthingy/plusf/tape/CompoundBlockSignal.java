package com.socialthingy.plusf.tape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundBlockSignal implements BlockSignal {
    private final List<BlockSignalProvider> blockSignalProviders = new ArrayList<>();
    private final SignalState signalState;
    private BlockSignal currentBlockSignal;
    private int currentProvider = 0;

    public CompoundBlockSignal(final SignalState signalState, final BlockSignalProvider... blockSignalProviders) {
        this.blockSignalProviders.addAll(Arrays.asList(blockSignalProviders));
        this.signalState = signalState;
        this.currentBlockSignal = blockSignalProviders[0].getBlockSignal(signalState);
    }

    public CompoundBlockSignal(final SignalState signalState, final List<BlockSignalProvider> blockSignalProviders) {
        this.blockSignalProviders.addAll(blockSignalProviders);
        this.signalState = signalState;
        this.currentBlockSignal = blockSignalProviders.get(0).getBlockSignal(signalState);
    }

    @Override
    public boolean hasNext() {
        if (currentBlockSignal != null) {
            if (currentBlockSignal.hasNext()) {
                return true;
            } else {
                currentBlockSignal = nextBlock();
                return hasNext();
            }
        } else {
            return false;
        }
    }

    @Override
    public Boolean next() {
        final Boolean nextBit = currentBlockSignal.next();
        if (!currentBlockSignal.hasNext()) {
            currentBlockSignal = nextBlock();
        }

        return nextBit;
    }

    public int skip(final int amount) {
        int remaining = amount;
        int skipped = 0;
        while (remaining > 0 && currentBlockSignal != null) {
            final int skippedInBlock = currentBlockSignal.skip(remaining);
            remaining -= skippedInBlock;
            skipped += skippedInBlock;
            if (!currentBlockSignal.hasNext()) {
                currentBlockSignal = nextBlock();
            }
        }

        return skipped;
    }

    private BlockSignal nextBlock() {
        if (blockSignalProviders == null) {
            return null;
        }

        currentProvider++;
        if (currentProvider >= blockSignalProviders.size() || currentProvider < 0) {
            return null;
        }

        final BlockSignalProvider nextBlockSignalProvider = blockSignalProviders.get(currentProvider);
        return nextBlockSignalProvider.getBlockSignal(signalState);
    }
}
