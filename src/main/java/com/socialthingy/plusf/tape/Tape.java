package com.socialthingy.plusf.tape;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tape {
    private String version;
    private TapeBlock[] blocks;

    public Tape(final String version, final List<TapeBlock> blocks) throws TapeException {
        this.version = version;
        this.blocks = blocks.toArray(new TapeBlock[blocks.size()]);
    }

    public String getVersion() {
        return version;
    }

    public TapeBlock[] getBlocks() {
        return blocks;
    }

    public List<Pair<String, String>> getArchiveInfo() {
        final List<Pair<String, String>> info = new ArrayList<>();
        for (TapeBlock block: blocks) {
            if (block instanceof ArchiveInfoBlock) {
                info.addAll(((ArchiveInfoBlock) block).getDescriptions());
            }
        }
        return info;
    }
}
