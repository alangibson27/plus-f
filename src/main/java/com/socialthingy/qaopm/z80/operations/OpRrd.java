package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

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
        final int[] memoryNibbles = Bitwise.nibbles(memory[address]);
        final int[] accumulatorNibbles = Bitwise.nibbles(accumulator.get());

        memory[address] = (accumulatorNibbles[1] << 4) + memoryNibbles[0];
        accumulator.set((accumulatorNibbles[0] << 4) + memoryNibbles[1]);

        setSignZeroAndParity(accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);

        return 18;
    }
}
