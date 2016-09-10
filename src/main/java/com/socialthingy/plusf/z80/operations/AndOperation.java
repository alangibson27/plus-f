package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

abstract class AndOperation implements Operation {
    protected final FlagsRegister flagsRegister;
    protected final Register accumulator;
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    AndOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    protected void and(final int value) {
        final int result = accumulator.set(accumulator.get() & value);
        flagsRegister.set(FlagsRegister.Flag.S, (result & 0b10000000) > 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(result));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, false);
        flagsRegister.setUndocumentedFlagsFromValue(result);
    }
}
