package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpCall extends CallOperation {
    public OpCall(final Processor processor, final Clock clock) {
        super(processor, clock);
    }

    @Override
    public void execute() {
        final int target = processor.fetchNextWord();
        call(target);
        clock.tick(1);
    }

    @Override
    public String toString() {
        return "call nn";
    }
}
