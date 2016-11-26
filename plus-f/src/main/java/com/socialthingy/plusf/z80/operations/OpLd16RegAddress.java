package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

public class OpLd16RegAddress implements Operation {
    private final Processor processor;
    private final int[] memory;
    private final Register dest;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpLd16RegAddress(final Processor processor, final int[] memory, final Register dest) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        dest.set(
            Word.from(
                unsafe.getInt(memory, 16L + (source * 4)),
                unsafe.getInt(memory, 16L + (((source + 1) & 0xffff) * 4))
            )
        );
        return 20;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (nn)", dest.name());
    }
}
