package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJpConditional implements Operation {
    private final Processor processor;
    private final FlagsRegister flags;
    private final Register pcReg;
    private final FlagsRegister.Flag flag;
    private final boolean whenSet;

    public OpJpConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean whenSet) {
        this.pcReg = processor.register("pc");
        this.flags = FlagsRegister.class.cast(processor.register("f"));
        this.processor = processor;
        this.flag = flag;
        this.whenSet = whenSet;
    }

    @Override
    public int execute() {
        final int destination = processor.fetchNextWord();
        if (flags.get(flag) == whenSet) {
            pcReg.set(destination);
        }
        return 10;
    }
}