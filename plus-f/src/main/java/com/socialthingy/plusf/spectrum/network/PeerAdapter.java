package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.*;
import com.socialthingy.plusf.spectrum.ui.ProgressDialog;
import com.socialthingy.plusf.util.ObservedValue;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_HOST;
import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_PORT;

public class PeerAdapter<T> implements Callbacks {
    private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress(DISCOVERY_HOST, DISCOVERY_PORT);

    private final Peer peer;
    private final Consumer<T> receiver;
    private Optional<ProgressDialog> connectionProgress = Optional.empty();
    private final ObservedValue<Boolean> connected = new ObservedValue<>(false);
    private final ObservedValue<Statistics> statistics = new ObservedValue<>(new Statistics(0, 0, 0, 0.0));
    private final ObservedValue<Long> timeSinceLastReceived = new ObservedValue<>(0L);

    private long lastReceivedTime = 0;

    public PeerAdapter(
        final Consumer<T> receiver,
        final int port,
        final Serialiser serialiser,
        final Deserialiser deserialiser
    ) {
        this.peer = new Peer(
            new InetSocketAddress(port),
            DISCOVERY_ADDR,
            this,
            serialiser,
            deserialiser,
            java.time.Duration.ofMinutes(1)
        );
        this.receiver = receiver;
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
            this::updateStatistics,
            10,
            10,
            TimeUnit.SECONDS
        );
    }

    public boolean isConnected() {
        return connected.get();
    }

    public ObservedValue<Boolean> connectedProperty() {
        return connected;
    }

    public void connect(final Window parent, final String sessionId, final Optional<Integer> forwardedPort) {
        final ProgressDialog progressMonitor = new ProgressDialog(
                parent,
                "Connecting to Peer",
                this::disconnect
        );
        progressMonitor.setVisible(true);

        connectionProgress = Optional.of(progressMonitor);
        peer.join(sessionId, forwardedPort);
    }

    public void reconnect(final InetSocketAddress destination) {
        peer.connectDirectly(destination.getPort(), destination.getHostString());
    }

    public void disconnect() {
        connected.set(false);
        peer.close();
    }

    public void updateStatistics() {
        if (connected.get()) {
            statistics.set(peer.statistics());
            timeSinceLastReceived.set(System.currentTimeMillis() - lastReceivedTime);
        }
    }

    public void send(final Object data) {
        if (data.getClass().isArray()) {
            final Object[] objs = (Object[]) data;
            for (int i = 0; i < objs.length; i++) {
                peer.send(new RawData(objs[i], i));
            }
        } else {
            peer.send(new RawData(data, 0));
        }
    }

    public void shutdown() {
        peer.close();
    }

    @Override
    public void waitingForPeer() {
        withConnectionDialog(cp -> cp.setMessage("Waiting for peer ..."));
    }

    @Override
    public void connectedToPeer(final InetSocketAddress address) {
        connected.set(true);
        withConnectionDialog(cp -> {
            cp.setMessage(String.format("Connected to peer on port %d", address.getPort()));
            cp.close();
            connectionProgress = Optional.empty();
        });
    }

    @Override
    public void discovering() {
        withConnectionDialog(cp -> cp.setMessage("Contacting discovery service ..."));
    }

    @Override
    public void initialising() {
        withConnectionDialog(cp -> cp.setMessage("Initialising peer ..."));
    }

    @Override
    public void closed() {
        connected.set(false);
    }

    @Override
    public void data(final Object content) {
        receiver.accept((T) content);
        this.lastReceivedTime = System.currentTimeMillis();
    }

    @Override
    public void discoveryTimeout() {
        withConnectionDialog(ProgressDialog::close);
        connectionProgress = Optional.empty();
        JOptionPane.showMessageDialog(
            null,
            "Peer discovery timed out. Try again later.",
            "Connection Timeout",
            JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void discoveryCancelled() {
    }

    private void withConnectionDialog(final Consumer<ProgressDialog> action) {
        connectionProgress.ifPresent(cp -> EventQueue.invokeLater(() -> action.accept(cp)));
    }

    public ObservedValue<Statistics> statistics() {
        return statistics;
    }

    public ObservedValue<Long> timeSinceLastReceived() {
        return timeSinceLastReceived;
    }
}
