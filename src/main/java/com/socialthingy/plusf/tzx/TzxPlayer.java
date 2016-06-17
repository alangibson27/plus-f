package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.util.Iterator;

public class TzxPlayer {
    private final Tzx tzx;

    public TzxPlayer(final Tzx tzx) {
        this.tzx = tzx;
    }

    public Iterator<TzxBlock.Bit> playBlock(final int idx) {
        final RepeatingList<TzxBlock.Bit> tape = new RepeatingList<>();
        if (idx < tzx.getBlocks().size()) {
            tzx.getBlocks().get(idx).write(tape);
        }

        if (idx == tzx.getBlocks().size() - 1) {
            tape.add(new TzxBlock.Bit(false, "end"), 3500);
        }

        return tape.iterator();
    }

    public Iterator<TzxBlock.Bit> getTape() {
        final RepeatingList<TzxBlock.Bit> tape = new RepeatingList<>();
        for (TzxBlock block: tzx.getBlocks()) {
            block.write(tape);
        }

        tape.add(new TzxBlock.Bit(false, "end"), 3500);
        return tape.iterator();
    }
}
