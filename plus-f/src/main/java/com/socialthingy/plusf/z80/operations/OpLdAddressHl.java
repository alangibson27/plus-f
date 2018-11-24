package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdAddressHl extends Operation {
    private final Processor processor;
    private final BytePairRegister hlReg;
    private final Memory memory;

    public OpLdAddressHl(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = processor.fetchNextWord();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address + 1, 3);
        memory.set(address, hlReg.getLow());
        memory.set(address + 1, hlReg.getHigh());
    }

    @Override
    public String toString() {
        return "ld (nn), hl";
    }

}
