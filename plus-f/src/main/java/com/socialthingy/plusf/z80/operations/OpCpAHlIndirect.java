package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpCpAHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock, false);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute() {
        sub(memory.get(hlReg.get()), false);
    }

    @Override
    public String toString() {
        return "cp (hl)";
    }
}

