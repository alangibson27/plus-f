package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.*;
import sun.misc.Unsafe;

import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;
import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

abstract class BlockOperation implements Operation {
    protected static final int LOOP_CYCLES = 21;
    protected static final int FINAL_CYCLES = 16;

    protected final FlagsRegister flagsRegister;
    protected final Register accumulator;
    protected final Register pcReg;
    protected final Register bcReg;
    protected final Register deReg;
    protected final Register hlReg;
    protected final int[] memory;
    protected final int increment;
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

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
            return LOOP_CYCLES;
        } else {
            return FINAL_CYCLES;
        }
    }

    protected void blockTransfer() {
        final int hlContents = unsafe.getInt(memory, BASE + (hlReg.get() * SCALE));
        final int undocumentedValue = (accumulator.get() + hlContents) & 0xff;
        Memory.set(memory, deReg.get(), hlContents);
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
        final int result = Bitwise.sub(accumulator.get(), unsafe.getInt(memory, BASE + (hlValue * SCALE)));
        hlReg.set(hlValue + increment);
        final int counter = bcReg.set(bcReg.get() - 1);

        final int answer = result & 0xff;
        final int halfCarry = (result & HALF_CARRY_BIT) == 0 ? 0 : 1;
        flagsRegister.set(FlagsRegister.Flag.S, (byte) answer < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, halfCarry == 1);
        flagsRegister.set(FlagsRegister.Flag.P, counter != 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        final int undocumentedValue = (accumulator.get() - unsafe.getInt(memory, BASE + (hlValue * SCALE)) - halfCarry);
        flagsRegister.set(FlagsRegister.Flag.F3, (undocumentedValue & 0b00001000) > 0);
        flagsRegister.set(FlagsRegister.Flag.F5, (undocumentedValue & 0b00000010) > 0);

        return answer;
    }
}
