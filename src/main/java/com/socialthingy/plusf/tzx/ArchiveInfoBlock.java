package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.stream.Collectors.*;

public class ArchiveInfoBlock extends TzxBlock {

    private static Map<Integer, String> textIds = new HashMap<>();
    static {
        textIds.put(0x00, "Title");
        textIds.put(0x01, "Publisher");
        textIds.put(0x02, "Author");
        textIds.put(0x03, "Year");
        textIds.put(0x04, "Language");
        textIds.put(0x05, "Type");
        textIds.put(0x06, "Price");
        textIds.put(0x07, "Loader");
        textIds.put(0x08, "Origin");
        textIds.put(0xff, "Comments");
    }

    private final List<String> descriptions;

    public static Try<ArchiveInfoBlock> read(final InputStream tzxFile) {
        try {
            final int length = nextWord(tzxFile);
            final int numStrings = nextByte(tzxFile);

            final List<String> descriptions = new ArrayList<>();
            for (int i = 0; i < numStrings; i++) {
                descriptions.add(readString(tzxFile));
            }

            return Try.success(new ArchiveInfoBlock(descriptions));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private static String readString(final InputStream tzxFile) throws IOException {
        final int id = nextByte(tzxFile);
        final String buf = getFixedLengthString(tzxFile);

        return String.format("[%s] - %s", Optional.of(textIds.get(id)).orElse("Unknown"), buf);
    }

    public ArchiveInfoBlock(final List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return descriptions.stream().collect(joining("\n"));
    }
}
