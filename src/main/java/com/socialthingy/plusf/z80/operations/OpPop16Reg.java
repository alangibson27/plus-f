package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpPop16Reg implements Operation {
    private final Processor processor;
    private final Register register;

    public OpPop16Reg(final Processor processor, final Register register) {
        this.processor = processor;
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(Word.from(processor.popByte(), processor.popByte()));
        return 10;
    }
}
