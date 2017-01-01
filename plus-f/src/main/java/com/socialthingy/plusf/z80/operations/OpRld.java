package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpRld extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRld(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int memoryValue = unsafe.getInt(memory, BASE + ((address) * SCALE));

        Memory.set(memory, address, (lowNibble(memoryValue) << 4) + lowNibble(accumulator.get()));
        accumulator.set((highNibble(accumulator.get()) << 4) + highNibble(memoryValue));

        setSignZeroAndParity(accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());

        return 18;
    }

    @Override
    public String toString() {
        return "rld";
    }
}
