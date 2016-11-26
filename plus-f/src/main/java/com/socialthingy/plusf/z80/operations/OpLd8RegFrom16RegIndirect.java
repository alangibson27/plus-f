package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

public class OpLd8RegFrom16RegIndirect implements Operation {

    private final Register dest;
    private final Register sourceReference;
    private final int[] memory;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpLd8RegFrom16RegIndirect(int[] memory, Register dest, Register sourceReference) {
        this.memory = memory;
        this.dest = dest;
        this.sourceReference = sourceReference;
    }

    @Override
    public int execute() {
        dest.set(unsafe.getInt(memory, 16L + (sourceReference.get() * 4)));
        return 7;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s)", dest.name(), sourceReference.name());
    }
}
