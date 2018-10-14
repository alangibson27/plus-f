package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.BytePairRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpLdHlAddress implements Operation {
    private final Processor processor;
    private final Memory memory;
    private final BytePairRegister hlReg;

    public OpLdHlAddress(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.memory = memory;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        hlReg.setLow(memory.get(source));
        hlReg.setHigh(memory.get((source + 1) & 0xffff));
        return 16;
    }

    @Override
    public String toString() {
        return "ld hl, (nn)";
    }
}
