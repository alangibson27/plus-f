package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpSubAIndexedIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;
    private final String toString;

    public OpSubAIndexedIndirect(final Processor processor, final int[] memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);

        if (useCarryFlag) {
            this.toString = "sbc a, (" + indexRegister.name() + " + n)";
        } else {
            this.toString = "sub a, (" + indexRegister.name() + " + n)";
        }
    }

    @Override
    public int execute() {
        accumulator.set(sub(unsafe.getInt(memory, BASE + ((indexRegister.withOffset(processor.fetchNextByte())) * SCALE)), true));
        return 19;
    }

    @Override
    public String toString() {
        return toString;
    }
}
