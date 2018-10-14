package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpOrAHlIndirect extends OrOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpOrAHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        or(memory.get(hlReg.get()));
        return 7;
    }

    @Override
    public String toString() {
        return "or (hl)";
    }
}
