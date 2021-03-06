package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

abstract class BlockOperation extends Operation {

    protected final FlagsRegister flagsRegister;
    private final Register accumulator;
    private final Register pcReg;
    private final Register bcReg;
    protected final Register deReg;
    protected final Register hlReg;
    private final Memory memory;
    private final int increment;

    protected BlockOperation(final Processor processor, final Memory memory, final int increment) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
        this.pcReg = processor.register("pc");
        this.bcReg = processor.register("bc");
        this.deReg = processor.register("de");
        this.hlReg = processor.register("hl");
        this.memory = memory;
        this.increment = increment;
    }

    protected boolean continueLoop() {
        if (bcReg.get() != 0x0000) {
            pcReg.set(pcReg.get() - 2);
            return true;
        } else {
            return false;
        }
    }

    protected void blockTransfer() {
        final int hlContents = memory.get(hlReg.get());
        final int undocumentedValue = (accumulator.get() + hlContents) & 0xff;
        memory.set(deReg.get(), hlContents);
        deReg.set(deReg.get() + increment);
        hlReg.set(hlReg.get() + increment);
        final int counter = bcReg.set(bcReg.get() - 1);

        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, counter != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.F3, (undocumentedValue & 0b00001000) > 0);
        flagsRegister.set(FlagsRegister.Flag.F5, (undocumentedValue & 0b00000010) > 0);
    }

    protected int blockCompare() {
        final int hlValue = hlReg.get();
        final int result = Bitwise.sub(accumulator.get(), memory.get(hlValue));
        hlReg.set(hlValue + increment);
        final int counter = bcReg.set(bcReg.get() - 1);

        final int answer = result & 0xff;
        final int halfCarry = (result & HALF_CARRY_BIT) == 0 ? 0 : 1;
        flagsRegister.set(FlagsRegister.Flag.S, (byte) answer < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, halfCarry == 1);
        flagsRegister.set(FlagsRegister.Flag.P, counter != 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        final int undocumentedValue = (accumulator.get() - memory.get(hlValue) - halfCarry);
        flagsRegister.set(FlagsRegister.Flag.F3, (undocumentedValue & 0b00001000) > 0);
        flagsRegister.set(FlagsRegister.Flag.F5, (undocumentedValue & 0b00000010) > 0);

        return answer;
    }
}
