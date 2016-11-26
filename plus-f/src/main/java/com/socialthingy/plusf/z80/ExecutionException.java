package com.socialthingy.plusf.z80;

public class ExecutionException extends Exception {
    private final Operation op;

    public ExecutionException(final Operation op, final Exception ex) {
        super(ex);
        this.op = op;
    }

    public Operation getOperation() {
        return op;
    }
}
