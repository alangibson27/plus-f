package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpCall extends CallOperation {
    public OpCall(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        call(processor.fetchNextWord());
        return 17;
    }

    @Override
    public String toString() {
        return "call nn";
    }
}
