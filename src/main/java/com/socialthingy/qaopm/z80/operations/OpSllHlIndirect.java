package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpSllHlIndirect extends SllOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpSllHlIndirect(final Processor processor, final int[] memory) {
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
