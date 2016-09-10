package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

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
        final int[] memoryNibbles = Bitwise.nibbles(unsafe.getInt(memory, 16L + ((address) * 4)));
        final int[] accumulatorNibbles = Bitwise.nibbles(accumulator.get());

        Memory.set(memory, address, (memoryNibbles[1] << 4) + accumulatorNibbles[1]);
        accumulator.set((accumulatorNibbles[0] << 4) + memoryNibbles[0]);

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
