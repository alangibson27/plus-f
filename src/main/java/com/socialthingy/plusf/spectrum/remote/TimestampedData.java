package com.socialthingy.plusf.spectrum.remote;

import com.google.common.primitives.Longs;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.DatagramPacket;
import java.util.function.Consumer;
import java.util.function.Function;

public class TimestampedData<T> {
    private final long timestamp;
    private final long systemTime;
    private final T data;

    public static <T> TimestampedData<T> from(
        final DatagramPacket data,
        final Function<InputStream, T> deserialiser
    ) throws IOException {
        try (ByteArrayInputStream bis =
                     new ByteArrayInputStream(data.getData(), data.getOffset(), data.getLength())) {
            return new TimestampedData<>(
                getLong(bis),
                getLong(bis),
                deserialiser.apply(bis)
            );
        }
    }

    private static final long getLong(final InputStream bis) throws IOException {
        return Longs.fromBytes(
            (byte) bis.read(), (byte) bis.read(), (byte) bis.read(), (byte) bis.read(),
            (byte) bis.read(), (byte) bis.read(), (byte) bis.read(), (byte) bis.read()
        );
    }

    public TimestampedData(final Long timestamp, final T data) {
        this.timestamp = timestamp;
        this.data = data;
        this.systemTime = System.currentTimeMillis();
    }

    private TimestampedData(final Long timestamp, final Long systemTime, final T data) {
        this.timestamp = timestamp;
        this.systemTime = systemTime;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public T getData() {
        return data;
    }

    public DatagramPacket toPacket(final Consumer<Pair<T, OutputStream>> serialiser) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(16384)) {
            bos.write(Longs.toByteArray(timestamp));
            bos.write(Longs.toByteArray(systemTime));
            serialiser.accept(Pair.of(data, bos));

            final byte[] serialised = bos.toByteArray();
            return new DatagramPacket(serialised, serialised.length);
        }
    }
}
