package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpCpAHlIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpCpAHlIndirect(final Processor processor, final int[] memory) {
        super(processor, false);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        sub(memory[hlReg.get()]);
        return 7;
    }
}

