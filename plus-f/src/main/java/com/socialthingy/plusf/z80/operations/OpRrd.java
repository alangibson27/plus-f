package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpRrd extends RotateOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpRrd(final Processor processor, final Memory memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);

        final int memoryValue = memory.get(address);

        memory.set( address, (lowNibble(accumulator.get()) << 4) + highNibble(memoryValue));
        accumulator.set((highNibble(accumulator.get()) << 4) + lowNibble(memoryValue));

        setSignZeroAndParity(accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return "rrd";
    }
}
