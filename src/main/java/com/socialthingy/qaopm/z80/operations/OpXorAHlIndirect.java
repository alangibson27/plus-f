package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpXorAHlIndirect extends XorOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpXorAHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        xor(memory[hlReg.get()]);
        return 7;
    }
}
