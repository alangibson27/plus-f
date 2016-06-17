package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TzxPlayer {
    private final List<TzxBlock> expandedBlocks = new ArrayList<>();

    public TzxPlayer(final Tzx tzx) throws TzxException {
        expandBlocks(tzx.getBlocks().toArray(new TzxBlock[tzx.getBlocks().size()]));
    }

    private void expandBlocks(final TzxBlock[] originalBlocks) throws TzxException {
        int idx = 0;
        while (idx < originalBlocks.length) {
            if (originalBlocks[idx] instanceof JumpBlock) {
                idx += ((JumpBlock) originalBlocks[idx]).getBlocks();
            } else if (originalBlocks[idx] instanceof LoopStartBlock) {
                final int loopEnd = findLoopEnd(originalBlocks, idx + 1);
                expandLoop(
                    ((LoopStartBlock) originalBlocks[idx]).getIterations(),
                    originalBlocks,
                    idx + 1,
                    loopEnd
                );

                idx = loopEnd;
            } else {
                expandedBlocks.add(originalBlocks[idx++]);
            }
        }
    }

    private void expandLoop(
        final int iterations,
        final TzxBlock[] originalBlocks,
        final int loopStart,
        final int loopEnd
    ) {
       for (int i = 0; i < iterations; i++) {
           for (int j = loopStart; j < loopEnd; j++) {
               expandedBlocks.add(originalBlocks[j]);
           }
       }
    }

    private int findLoopEnd(final TzxBlock[] originalBlocks, final int from) throws TzxException {
        for (int i = from; i < originalBlocks.length; i++) {
            if (originalBlocks[i] instanceof LoopEndBlock) {
                return i;
            }
        }

        throw new TzxException("Unterminated loop");
    }

    public PlayableTzx getPlayableTzx() {
        final RepeatingList<TzxBlock.Bit> tape = new RepeatingList<>();
        boolean state = true;
        for (TzxBlock block: expandedBlocks) {
            state = block.write(tape, state);
        }
        tape.add(new TzxBlock.Bit(false, "end"), 3500);

        return new PlayableTzx(tape.iterator());
    }

    public Iterator<TzxBlock.Bit> playBlock(final int idx) {
        final RepeatingList<TzxBlock.Bit> tape = new RepeatingList<>();
        if (idx < expandedBlocks.size()) {
            expandedBlocks.get(idx).write(tape, true);
        }

        if (idx == expandedBlocks.size() - 1) {
            tape.add(new TzxBlock.Bit(false, "end"), 3500);
        }

        tape.add(new TzxBlock.Bit(false, "end"), 3500);
        return tape.iterator();
    }
}
