package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.RepeatingList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tape {
    private String version;
    private TapeBlock[] blocks;

    public Tape(final String version, final List<TapeBlock> blocks) throws TapeException {
        this.version = version;
        this.blocks = blocks.toArray(new TapeBlock[blocks.size()]);
    }

    public String getVersion() {
        return version;
    }

    public TapeBlock[] getBlocks() {
        return blocks;
    }

    public Iterator<TapeBlock.Bit> getPlayableTape() throws TapeException {
        final RepeatingList<TapeBlock.Bit> tape = new RepeatingList<>();
        boolean state = false;
        final List<TapeBlock> expandedBlocks = expandBlocks();
        for (TapeBlock block: expandedBlocks) {
            state = block.write(tape, state);
        }
        tape.add(new TapeBlock.Bit(state, "end"), 3500);
        tape.add(new TapeBlock.Bit(false, "end"), 3500);

        return tape.iterator();
    }

    private List<TapeBlock> expandBlocks() throws TapeException {
        final List<TapeBlock> expandedBlocks = new ArrayList<>();
        int idx = 0;
        while (idx < blocks.length) {
            if (blocks[idx] instanceof JumpBlock) {
                idx += ((JumpBlock) blocks[idx]).getBlocks();
            } else if (blocks[idx] instanceof LoopStartBlock) {
                final int loopEnd = findLoopEnd(blocks, idx + 1);
                expandLoop(
                        ((LoopStartBlock) blocks[idx]).getIterations(),
                        blocks,
                        idx + 1,
                        loopEnd,
                        expandedBlocks
                );

                idx = loopEnd;
            } else {
                expandedBlocks.add(blocks[idx++]);
            }
        }

        return expandedBlocks;
    }

    private void expandLoop(
            final int iterations,
            final TapeBlock[] originalBlocks,
            final int loopStart,
            final int loopEnd,
            final List<TapeBlock> expandedBlocks
    ) {
        for (int i = 0; i < iterations; i++) {
            for (int j = loopStart; j < loopEnd; j++) {
                expandedBlocks.add(originalBlocks[j]);
            }
        }
    }

    private int findLoopEnd(final TapeBlock[] originalBlocks, final int from) throws TapeException {
        for (int i = from; i < originalBlocks.length; i++) {
            if (originalBlocks[i] instanceof LoopEndBlock) {
                return i;
            }
        }

        throw new TapeException("Unterminated loop");
    }
}
