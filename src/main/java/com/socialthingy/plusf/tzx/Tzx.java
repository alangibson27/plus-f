package com.socialthingy.plusf.tzx;

import java.util.List;

public class Tzx {
    private String version;
    private List<TzxBlock> blocks;

    public Tzx(final String version, final List<TzxBlock> blocks) {
        this.version = version;
        this.blocks = blocks;
    }

    public String getVersion() {
        return version;
    }

    public List<TzxBlock> getBlocks() {
        return blocks;
    }
}
