package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSetHlIndirect extends BitModificationOperation {

    private final Register hlReg;
    private final int[] memory;

    public OpSetHlIndirect(final Processor processor, final int[] memory, final int bitPosition) {
        super(bitPosition);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        memory[address] = set(memory[address]);
        return 15;
    }
}
