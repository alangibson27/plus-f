package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpCpAHlIndirect(final Processor processor, final Memory memory) {
        super(processor, false);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        sub(memory.get(hlReg.get()), false);
        return 7;
    }

    @Override
    public String toString() {
        return "cp (hl)";
    }
}

