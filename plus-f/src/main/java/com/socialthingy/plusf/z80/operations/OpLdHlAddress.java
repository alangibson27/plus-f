package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.BytePairRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import sun.misc.Unsafe;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpLdHlAddress implements Operation {
    private final Processor processor;
    private final int[] memory;
    private final BytePairRegister hlReg;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpLdHlAddress(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        hlReg.setLow(unsafe.getInt(memory, BASE + ((source) * SCALE)));
        hlReg.setHigh(unsafe.getInt(memory, BASE + (((source + 1) & 0xffff) * SCALE)));
        return 16;
    }

    @Override
    public String toString() {
        return "ld hl, (nn)";
    }
}
