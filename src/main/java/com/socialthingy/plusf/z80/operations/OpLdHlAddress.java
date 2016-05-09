package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.BytePairRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpLdHlAddress implements Operation {
    private final Processor processor;
    private final int[] memory;
    private final BytePairRegister hlReg;

    public OpLdHlAddress(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        hlReg.setLow(memory[source]);
        hlReg.setHigh(memory[(source + 1) & 0xffff]);
        return 16;
    }
}
