package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSubAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpSubAHlIndirect(final Processor processor, final Memory memory, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public int execute() {
        accumulator.set(sub(memory.get(hlReg.get()), true));
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
        return 7;
    }

    @Override
    public String toString() {
        return useCarryFlag ? "sbc a, (hl)" : "sub (hl)";
    }
}
