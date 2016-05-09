package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRlcHlIndirect extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRlcHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = rlcValue(memory[address]);
        setSignZeroAndParity(result);
        memory[address] = result;
        return 15;
    }
}
