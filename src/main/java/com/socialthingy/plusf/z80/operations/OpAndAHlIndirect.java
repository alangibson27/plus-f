package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

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

    @Override
    public String toString() {
        return "and (hl)";
    }
}
