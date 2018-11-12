package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpXorAImmediate extends XorOperation {

    private final Processor processor;

    public OpXorAImmediate(final Processor processor, final Clock clock) {
        super(processor, clock);
        this.processor = processor;
    }

    @Override
    public void execute() {
        xor(processor.fetchNextByte());
        clock.tick(3);
    }

    @Override
    public String toString() {
        return "xor n";
    }
}
