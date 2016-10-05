package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrd extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRrd(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int memoryValue = unsafe.getInt(memory, 16L + ((address) * 4));

        Memory.set(memory, address, (lowNibble(accumulator.get()) << 4) + highNibble(memoryValue));
        accumulator.set((highNibble(accumulator.get()) << 4) + lowNibble(memoryValue));

        setSignZeroAndParity(accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());

        return 18;
    }

    @Override
    public String toString() {
        return "rrd";
    }
}
