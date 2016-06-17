package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class TextDescriptionBlock extends TzxBlock {

    private final String description;

    public static Try<TextDescriptionBlock> read(final InputStream tzxFile) {
        try {
            final String description = getFixedLengthString(tzxFile);
            return Try.success(new TextDescriptionBlock(description));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public TextDescriptionBlock(final String description) {
        this.description = description;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return description;
    }
}
