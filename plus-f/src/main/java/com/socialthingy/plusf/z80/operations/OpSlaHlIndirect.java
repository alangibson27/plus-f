package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSlaHlIndirect extends SlaOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpSlaHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        Memory.set(memory, address, shift(unsafe.getInt(memory, 16L + ((address) * 4))));
        return 15;
    }

    @Override
    public String toString() {
        return "sla (hl)";
    }
}
