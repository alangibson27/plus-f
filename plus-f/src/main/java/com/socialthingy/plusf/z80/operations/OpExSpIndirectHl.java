package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

public class OpExSpIndirectHl implements Operation {

    private final Register spReg;
    private final Register hReg;
    private final Register lReg;
    private final int[] memory;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpExSpIndirectHl(final Processor processor, final int[] memory) {
        this.spReg = processor.register("sp");
        this.hReg = processor.register("h");
        this.lReg = processor.register("l");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int oldH = hReg.get();
        final int oldL = lReg.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        lReg.set(unsafe.getInt(memory, 16L + (spLow * 4)));
        hReg.set(unsafe.getInt(memory, 16L + (spHigh * 4)));

        Memory.set(memory, spLow, oldL);
        Memory.set(memory, spHigh, oldH);
        return 19;
    }

    @Override
    public String toString() {
        return "ex (sp), hl";
    }
}