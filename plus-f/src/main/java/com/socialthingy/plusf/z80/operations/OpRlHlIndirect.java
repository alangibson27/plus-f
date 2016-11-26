package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRlHlIndirect extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRlHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = rlValue(unsafe.getInt(memory, 16L + ((address) * 4)));
        setSignZeroAndParity(result);
        Memory.set(memory, address, result);
        return 15;
    }

    @Override
    public String toString() {
        return "rl (hl)";
    }
}