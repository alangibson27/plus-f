package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdHlAddress extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final BytePairRegister hlReg;

    public OpLdHlAddress(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
    }

    @Override
    public void execute() {
        final int source = processor.fetchNextWord();
        hlReg.setLow(memory.get(source));
        hlReg.setHigh(memory.get((source + 1) & 0xffff));
        clock.tick(12);
    }

    @Override
    public String toString() {
        return "ld hl, (nn)";
    }
}
