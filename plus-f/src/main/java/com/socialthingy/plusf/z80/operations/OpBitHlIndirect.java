package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpBitHlIndirect extends BitOperation {

    private final int[] memory;
    private final Register hlReg;
    private final Register mysteryReg;
    private final String toString;

    public OpBitHlIndirect(final Processor processor, final int[] memory, final int bitPosition) {
        super(processor, bitPosition);
        this.memory = memory;
        this.hlReg = processor.register("hl");
        this.mysteryReg = processor.register("?");

        this.toString = String.format("bit %d, (hl)", bitPosition);
    }

    @Override
    public int execute() {
        checkBit(unsafe.getInt(memory, BASE + (hlReg.get() * SCALE)));
        flagsRegister.setUndocumentedFlagsFromValue(mysteryReg.get());
        return 12;
    }

    @Override
    public String toString() {
        return toString;
    }
}
