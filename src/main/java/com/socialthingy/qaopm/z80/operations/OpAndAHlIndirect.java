package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpAndAHlIndirect extends AndOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpAndAHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        and(memory[hlReg.get()]);
        return 7;
    }
}
