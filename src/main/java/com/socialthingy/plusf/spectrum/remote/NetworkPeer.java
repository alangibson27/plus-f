package com.socialthingy.plusf.spectrum.remote;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NetworkPeer<R, S> {
    private final Consumer<R> updater;
    private final Consumer<Pair<S, OutputStream>> serialiser;
    private final Function<InputStream, R> deserialiser;
    private final Supplier<Long> timestamper;
    private final DatagramSocket socket;
    private final AtomicLong lastReceived = new AtomicLong(-1);
    private final ScheduledExecutorService receiveExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Timer latencyTimer;
    private final Counter outOfOrderCounter;
    private boolean active = true;
    private final SocketAddress partner;

    public NetworkPeer(
        final Consumer<R> updater,
        final Consumer<Pair<S, OutputStream>> serialiser,
        final Function<InputStream, R> deserialiser,
        final Supplier<Long> timestamper,
        final Integer localPort,
        final SocketAddress partner
    ) throws SocketException {
        this.updater = updater;
        this.timestamper = timestamper;
        this.socket = new DatagramSocket(localPort);
        this.partner = partner;
        this.serialiser = serialiser;
        this.deserialiser = deserialiser;

        final MetricRegistry metricRegistry = new MetricRegistry();
        this.latencyTimer = metricRegistry.timer("latency");
        this.outOfOrderCounter = metricRegistry.counter("outOfOrder");
        this.receiveExecutor.schedule(new PartnerDataReceiver(), 0, TimeUnit.SECONDS);
    }

    public boolean awaitingCommunication() {
        return lastReceived.get() < 0;
    }

    public void sendDataToPartner(final S data) {
        sendExecutor.execute(() -> {
            final TimestampedData<S> tsData = new TimestampedData<>(timestamper.get(), data);
            try {
                final DatagramPacket packet = tsData.toPacket(serialiser);
                packet.setSocketAddress(partner);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void receivePartnerData(final TimestampedData<R> hostData) {
        final long sentTimestamp = hostData.getTimestamp();
        if (sentTimestamp > lastReceived.get()) {
            final long latency = System.currentTimeMillis() - hostData.getSystemTime();
            lastReceived.set(sentTimestamp);
            updater.accept(hostData.getData());
            latencyTimer.update(latency, MILLISECONDS);
        } else {
            outOfOrderCounter.inc();
        }
    }

    public SocketAddress getLocalAddress() {
        return this.socket.getLocalSocketAddress();
    }

    public void disconnect() {
        active = false;
        this.socket.close();
        this.receiveExecutor.shutdownNow();
        this.sendExecutor.shutdownNow();
    }

    public double getAverageLatency() {
        return latencyTimer.getSnapshot().getMean() / 1000000.0;
    }

    public long getOutOfOrderPacketCount() {
        return outOfOrderCounter.getCount();
    }

    private class PartnerDataReceiver implements Runnable {
        @Override
        public void run() {
            final DatagramPacket dp = new DatagramPacket(new byte[0x10000], 0x10000);
            while (active) {
                try {
                    socket.receive(dp);
                    final TimestampedData<R> data = TimestampedData.from(dp, deserialiser);
                    receivePartnerData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
