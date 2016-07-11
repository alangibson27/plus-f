package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class GroupStartBlock extends TapeBlock {

    private final String description;

    public static Try<GroupStartBlock> read(final InputStream tzxFile) {
        try {
            final String message = getFixedLengthString(tzxFile);

            return Try.success(new GroupStartBlock(message));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public GroupStartBlock(final String description) {
        this.description = description;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return String.format("Group start: %s", description);
    }
}
