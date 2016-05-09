package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.FlagsRegister.Flag;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJrConditional implements Operation {

    private final Processor processor;
    private final Register pcReg;
    private final FlagsRegister flagsRegister;
    private final Flag flag;
    private final boolean whenSet;

    public OpJrConditional(final Processor processor, final Flag flag, final boolean whenSet) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.flagsRegister = FlagsRegister.class.cast(processor.register("f"));
        this.flag = flag;
        this.whenSet = whenSet;
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextPC();
        if (flagsRegister.get(flag) == whenSet) {
            pcReg.set(pcReg.get() + offset);
            return 12;
        } else {
            return 7;
        }
    }
}
