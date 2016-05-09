package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpExSpIndirectIndexed implements Operation {

    private final Register spReg;
    private final IndexRegister indexRegister;
    private final int[] memory;

    public OpExSpIndirectIndexed(final Processor processor, final Register indexRegister, final int[] memory) {
        this.spReg = processor.register("sp");
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int oldIndex = indexRegister.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        indexRegister.set(Word.from(memory[spLow], memory[spHigh]));

        memory[spLow] = oldIndex & 0x00ff;
        memory[spHigh] = (oldIndex & 0xff00) >> 8;
        return 23;
    }
}
