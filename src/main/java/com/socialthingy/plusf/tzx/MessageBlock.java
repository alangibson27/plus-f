package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

public class MessageBlock extends TzxBlock {

    private final String description;
    private final Duration duration;

    public static Try<MessageBlock> read(final InputStream tzxFile) {
        try {
            final Duration duration = Duration.ofSeconds(nextByte(tzxFile));
            final String message = getFixedLengthString(tzxFile);

            return Try.success(new MessageBlock(message, duration));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public MessageBlock(final String description, final Duration duration) {
        this.description = description;
        this.duration = duration;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return String.format("[%d seconds] %s", duration.getSeconds(), description);
    }
}
