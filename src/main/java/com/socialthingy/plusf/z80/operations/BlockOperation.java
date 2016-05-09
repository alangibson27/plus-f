package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

abstract class BlockOperation implements Operation {

    protected final FlagsRegister flagsRegister;
    private final Register accumulator;
    private final Register pcReg;
    private final Register bcReg;
    private final Register deReg;
    private final Register hlReg;
    private final int[] memory;
    private final int increment;

    protected BlockOperation(final Processor processor, final int[] memory, final int increment) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
        this.pcReg = processor.register("pc");
        this.bcReg = processor.register("bc");
        this.deReg = processor.register("de");
        this.hlReg = processor.register("hl");
        this.memory = memory;
        this.increment = increment;
    }

    protected int adjustPC() {
        if (bcReg.get() != 0x0000) {
            pcReg.set(pcReg.get() - 2);
            return 21;
        } else {
            return 16;
        }
    }

    protected void blockTransfer() {
        memory[deReg.get()] = memory[hlReg.get()];
        deReg.set(deReg.get() + increment);
        hlReg.set(hlReg.get() + increment);
        final int counter = bcReg.set(bcReg.get() - 1);

        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, counter != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
    }

    protected int blockCompare() {
        final int hlValue = hlReg.get();
        final int[] result = Bitwise.sub(accumulator.get(), memory[hlValue]);
        hlReg.set(hlValue + increment);
        final int counter = bcReg.set(bcReg.get() - 1);

        flagsRegister.set(FlagsRegister.Flag.S, (byte) result[0] < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.P, counter != 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        return result[0];
    }
}
