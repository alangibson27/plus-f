package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpPop16Reg extends Operation {
    private final Processor processor;
    private final Register register;

    public OpPop16Reg(final Processor processor, final Clock clock, final Register register) {
        super(clock);
        this.processor = processor;
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(Word.from(processor.popByte(), processor.popByte()));
    }

    @Override
    public String toString() {
        return "pop " + register.name();
    }
}
