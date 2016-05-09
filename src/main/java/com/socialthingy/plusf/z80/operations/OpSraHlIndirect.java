package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSraHlIndirect extends SraOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpSraHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        memory[address] = shift(memory[address]);
        return 15;
    }
}
