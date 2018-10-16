package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSraHlIndirect extends SraOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpSraHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        memory.set( address, shift(memory.get(address)));
        return 15;
    }

    @Override
    public String toString() {
        return "sra (hl)";
    }
}
