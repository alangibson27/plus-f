package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpDecHlIndirect extends DecOperation {

    private final Register hlReg;
    private final Memory memory;

    public OpDecHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = decrement(memory.get(address));
        memory.set( address, result);
        return 11;
    }

    @Override
    public String toString() {
        return "dec (hl)";
    }
}
