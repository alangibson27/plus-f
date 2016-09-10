package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import sun.misc.Unsafe;

abstract class DecOperation implements Operation {

    private final FlagsRegister flagsRegister;
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    protected DecOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    protected int decrement(final int initialValue) {
        final int[] result = Bitwise.sub(initialValue, 1);

        flagsRegister.set(FlagsRegister.Flag.P, initialValue == 0x80);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result[0] < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        flagsRegister.setUndocumentedFlagsFromValue(result[0]);
        return result[0];
    }
}
