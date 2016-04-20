package com.socialthingy.qaopm.spectrum.remote;

import com.codahale.metrics.*;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Guest {
    private final AtomicLong lastReceived = new AtomicLong(-1);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Supplier<Long> timestamper;
    private final DatagramSocket socket;
    private final Consumer<SpectrumState> guestUpdater;
    private final Timer latencyTimer;
    private final Counter outOfOrderCounter;

    private boolean active = true;

    public Guest(
        final Supplier<Long> timestamper,
        final SocketAddress host,
        final Consumer<SpectrumState> guestUpdater
    ) throws SocketException{
        this.timestamper = timestamper;
        this.guestUpdater = guestUpdater;

        this.socket = new DatagramSocket(7001);
        this.socket.connect(host);

        final MetricRegistry metricRegistry = new MetricRegistry();
        this.latencyTimer = metricRegistry.timer("latency");
        this.outOfOrderCounter = metricRegistry.counter("outOfOrder");

        this.executor.schedule(new HostDataReceiver(), 0, TimeUnit.SECONDS);
    }

    public SocketAddress getLocalAddress() {
        return this.socket.getLocalSocketAddress();
    }

    public void disconnectFromHost() {
        active = false;
        this.socket.close();
        this.executor.shutdownNow();
    }

    public void receiveHostData(final TimestampedData<SpectrumState> hostData) {
        final long sentTimestamp = hostData.getTimestamp();
        if (sentTimestamp > lastReceived.get()) {
            final long latency = timestamper.get() - sentTimestamp;
            lastReceived.set(sentTimestamp);
            guestUpdater.accept(hostData.getData());
            latencyTimer.update(latency, MILLISECONDS);
        } else {
            outOfOrderCounter.inc();
        }
    }

    public void sendKeyToHost(final KeyEvent keyEvent) {
        final TimestampedData<KeyEvent> data = new TimestampedData<>(timestamper.get(), keyEvent);
        final DatagramPacket packet = data.toPacket();
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAverageLatency() {
        return latencyTimer.getSnapshot().getMean() / 1000000.0;
    }

    public long getOutOfOrderPacketCount() {
        return outOfOrderCounter.getCount();
    }

    private class HostDataReceiver implements Runnable {
        @Override
        public void run() {
            final DatagramPacket dp = new DatagramPacket(new byte[16384], 16384);
            while (active) {
                try {
                    socket.receive(dp);
                    final TimestampedData<SpectrumState> data = TimestampedData.from(dp);
                    receiveHostData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
