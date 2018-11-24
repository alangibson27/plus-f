package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdHlAddress extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final BytePairRegister hlReg;

    public OpLdHlAddress(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.memory = memory;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int source = processor.fetchNextWord();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(source, 3);
        contentionModel.applyContention(source + 1, 3);

        hlReg.setLow(memory.get(source));
        hlReg.setHigh(memory.get((source + 1) & 0xffff));
    }

    @Override
    public String toString() {
        return "ld hl, (nn)";
    }
}
