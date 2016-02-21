package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpResHlIndirect extends BitModificationOperation {

    private final Register hlReg;
    private final int[] memory;

    public OpResHlIndirect(final Processor processor, final int[] memory, final int bitPosition) {
        super(bitPosition);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        memory[address] = reset(memory[address]);
        return 15;
    }
}
