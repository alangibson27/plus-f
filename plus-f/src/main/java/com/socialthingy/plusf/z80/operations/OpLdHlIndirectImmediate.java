package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdHlIndirectImmediate extends Operation {
    private final Processor processor;
    private final Register destReference;
    private final Memory memory;

    public OpLdHlIndirectImmediate(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.processor = processor;
        this.destReference = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute() {
        memory.set(destReference.get(), processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return "ld (hl), n";
    }
}
