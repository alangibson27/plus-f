package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrcHlIndirect extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRrcHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = rrcValue(memory[address]);
        setSignZeroAndParity(result);
        memory[address] = result;
        return 15;
    }
}
