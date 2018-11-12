package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpRld extends RotateOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpRld(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        final int memoryValue = memory.get(address);

        memory.set( address, (lowNibble(memoryValue) << 4) + lowNibble(accumulator.get()));
        accumulator.set((highNibble(accumulator.get()) << 4) + highNibble(memoryValue));

        setSignZeroAndParity(accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());

        clock.tick(10);
    }

    @Override
    public String toString() {
        return "rld";
    }
}
