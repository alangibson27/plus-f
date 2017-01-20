package com.socialthingy.plusf.z80;

public interface Operation {
    int execute();
    default boolean hasOptimisedForm() {
        return false;
    }
    default int executeOptimised(int cyclesRemaining) {
        return 0;
    }
}
