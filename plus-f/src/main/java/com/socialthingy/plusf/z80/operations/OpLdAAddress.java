package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpLdAAddress implements Operation {

    private final Processor processor;
    private final Register aReg;
    private final int[] memory;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpLdAAddress(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        final int address = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        aReg.set(unsafe.getInt(memory, BASE + (address * SCALE)));
        return 13;
    }

    @Override
    public String toString() {
        return "ld a, (nn)";
    }
}
