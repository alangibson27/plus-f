package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpLdir extends BlockOperation {
    private RRegister rReg;

    public OpLdir(final Processor processor, final int[] memory) {
        super(processor, memory, 1);
        this.rReg = (RRegister) processor.register("r");
    }

    @Override
    public boolean hasOptimisedForm() {
        return true;
    }

    @Override
    public int execute() {
        blockTransfer();
        flagsRegister.set(FlagsRegister.Flag.P, false);
        return adjustPC();
    }

    @Override
    public int executeOptimised(final int cyclesAvailable) {
        int repetitions = bcReg.get();
        int cyclesUsed = 0;
        int hlValue = 0;
        int loops = 0;
        final int hlAddr = hlReg.get();
        final int deAddr = deReg.get();
        do {
            if (cyclesUsed >= cyclesAvailable) {
                break;
            }

            if (repetitions == 1) {
                cyclesUsed += FINAL_CYCLES;
            } else {
                cyclesUsed += LOOP_CYCLES;
            }

            hlValue = unsafe.getInt(memory, BASE + (((hlAddr + loops) & 0xffff) * SCALE));
            Memory.set(memory, (deAddr + loops) & 0xffff, hlValue);

            repetitions = (repetitions - 1) & 0xffff;
            loops++;
        } while (repetitions != 0);

        if (repetitions != 0) {
            pcReg.set(pcReg.get() - 2);
        }
        hlReg.set(hlReg.get() + loops);
        deReg.set(deReg.get() + loops);
        bcReg.set(repetitions);
        rReg.increment((loops - 1) * 2);

        final int undocumentedValue = (accumulator.get() + hlValue) & 0xff;
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.F3, (undocumentedValue & 0b00001000) > 0);
        flagsRegister.set(FlagsRegister.Flag.F5, (undocumentedValue & 0b00000010) > 0);
        return cyclesUsed;
    }

    @Override
    public String toString() {
        return "ldir";
    }
}
