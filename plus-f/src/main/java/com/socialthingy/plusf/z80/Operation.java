package com.socialthingy.plusf.z80;

public abstract class Operation {
    public abstract void execute(ContentionModel contentionModel, int initialPcValue, int irValue);
}
