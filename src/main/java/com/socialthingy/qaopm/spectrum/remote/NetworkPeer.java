package com.socialthingy.qaopm.spectrum.remote;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NetworkPeer<R extends Serializable> {
    private final Consumer<R> updater;
    protected final Supplier<Long> timestamper;
    protected final DatagramSocket socket;
    private final AtomicLong lastReceived = new AtomicLong(-1);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Timer latencyTimer;
    private final Counter outOfOrderCounter;
    private boolean active = true;
    private final SocketAddress partner;

    public NetworkPeer(
        final Consumer<R> updater,
        final Supplier<Long> timestamper,
        final Integer localPort,
        final SocketAddress partner
    ) throws SocketException {
        this.updater = updater;
        this.timestamper = timestamper;
        this.socket = new DatagramSocket(localPort);
        this.partner = partner;

        final MetricRegistry metricRegistry = new MetricRegistry();
        this.latencyTimer = metricRegistry.timer("latency");
        this.outOfOrderCounter = metricRegistry.counter("outOfOrder");
        this.executor.schedule(new PartnerDataReceiver(), 0, TimeUnit.SECONDS);
    }

    public boolean awaitingCommunication() {
        return lastReceived.get() < 0;
    }

    public void sendDataToPartner(final Serializable data) {
        final TimestampedData<Serializable> tsData = new TimestampedData<>(timestamper.get(), data);
        final DatagramPacket packet = tsData.toPacket();
        try {
            packet.setSocketAddress(partner);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receivePartnerData(final TimestampedData<R> hostData) {
        final long sentTimestamp = hostData.getTimestamp();
        if (sentTimestamp > lastReceived.get()) {
            final long latency = timestamper.get() - sentTimestamp;
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
        this.executor.shutdownNow();
    }

    public double getAverageLatency() {
        return latencyTimer.getSnapshot().getMean() / 1000000.0;
    }

    public long getOutOfOrderPacketCount() {
        return outOfOrderCounter.getCount();
    }

    protected class PartnerDataReceiver implements Runnable {
        @Override
        public void run() {
            final DatagramPacket dp = new DatagramPacket(new byte[0x10000], 0x10000);
            while (active) {
                try {
                    socket.receive(dp);
                    final TimestampedData<R> data = TimestampedData.from(dp);
                    receivePartnerData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
