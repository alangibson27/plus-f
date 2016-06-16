package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpIncHlIndirect extends IncOperation {

    private final Register hlReg;
    private final int[] memory;

    public OpIncHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = increment(memory[address]);
        Memory.set(memory, address, result);
        return 11;
    }
}
