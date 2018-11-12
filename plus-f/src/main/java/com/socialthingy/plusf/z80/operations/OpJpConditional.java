package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJpConditional extends Operation {
    private final Processor processor;
    private final FlagsRegister flags;
    private final Register pcReg;
    private final FlagsRegister.Flag flag;
    private final boolean whenSet;
    private final String toString;

    public OpJpConditional(final Processor processor, final Clock clock, final FlagsRegister.Flag flag, final boolean whenSet) {
        super(clock);
        this.pcReg = processor.register("pc");
        this.flags = FlagsRegister.class.cast(processor.register("f"));
        this.processor = processor;
        this.flag = flag;
        this.whenSet = whenSet;

        if (whenSet) {
            this.toString = "jp " + flag.name().toLowerCase() + ", nn";
        } else {
            this.toString = "jp n" + flag.name().toLowerCase() + ", nn";
        }
    }

    @Override
    public void execute() {
        final int destination = processor.fetchNextWord();
        if (flags.get(flag) == whenSet) {
            pcReg.set(destination);
        }
        clock.tick(6);
    }

    @Override
    public String toString() {
        return toString;
    }
}
