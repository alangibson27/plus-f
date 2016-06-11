package com.socialthingy.plusf.spectrum.remote;

import com.google.common.primitives.Longs;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.DatagramPacket;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressedTimestampedData<T> {
    private final long timestamp;
    private final long systemTime;
    private final T data;
    private final int size;

    public static <T> CompressedTimestampedData<T> from(
        final DatagramPacket data,
        final Function<InputStream, T> deserialiser
    ) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data.getData(), data.getOffset(), data.getLength());
             InflaterInputStream iis = new InflaterInputStream(bis)) {
            return new CompressedTimestampedData<>(
                getLong(iis),
                getLong(iis),
                deserialiser.apply(iis),
                data.getLength()
            );
        }
    }

    private static final long getLong(final InputStream bis) throws IOException {
        return Longs.fromBytes(
            (byte) bis.read(), (byte) bis.read(), (byte) bis.read(), (byte) bis.read(),
            (byte) bis.read(), (byte) bis.read(), (byte) bis.read(), (byte) bis.read()
        );
    }

    public CompressedTimestampedData(final Long timestamp, final T data) {
        this.timestamp = timestamp;
        this.data = data;
        this.systemTime = System.currentTimeMillis();
        this.size = 0;
    }

    private CompressedTimestampedData(final Long timestamp, final Long systemTime, final T data, final int size) {
        this.timestamp = timestamp;
        this.systemTime = systemTime;
        this.data = data;
        this.size = size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public long getSize() {
        return size;
    }

    public T getData() {
        return data;
    }

    public DatagramPacket toPacket(final Consumer<Pair<T, OutputStream>> serialiser) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(16384)) {
            try (final DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
                dos.write(Longs.toByteArray(timestamp));
                dos.write(Longs.toByteArray(systemTime));
                serialiser.accept(Pair.of(data, dos));
            }
            final byte[] serialised = bos.toByteArray();
            return new DatagramPacket(serialised, serialised.length);
        }
    }
}
