package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdHlIndirectImmediate extends Operation {
    private final Processor processor;
    private final Register destReference;
    private final Memory memory;

    public OpLdHlIndirectImmediate(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.destReference = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int addr = destReference.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(addr, 3);
        memory.set(addr, processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return "ld (hl), n";
    }
}
