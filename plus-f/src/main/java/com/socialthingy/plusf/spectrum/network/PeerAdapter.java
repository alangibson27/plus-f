package com.socialthingy.plusf.spectrum.network;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.OnSuccess;
import com.socialthingy.plusf.p2p.*;
import com.socialthingy.plusf.spectrum.dialog.ErrorDialog;
import com.socialthingy.plusf.spectrum.ui.ProgressDialog;
import javafx.beans.property.*;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static akka.actor.ActorRef.noSender;
import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_HOST;
import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_PORT;

public class PeerAdapter<T> implements Callbacks {
    private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress(DISCOVERY_HOST, DISCOVERY_PORT);

    private final ActorSystem actorSystem;
    private final ActorRef peer;
    private final Consumer<T> receiver;
    private Optional<ProgressDialog> connectionProgress = Optional.empty();
    private final SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private final ObjectProperty<Statistics> statistics = new SimpleObjectProperty<>(Statistics.apply(0, 0, 0, 0.0));
    private final LongProperty timeSinceLastReceived = new SimpleLongProperty(0);

    private long lastReceivedTime = 0;

    public PeerAdapter(
        final ActorSystem actorSystem,
        final Consumer<T> receiver,
        final int port,
        final Serialiser serialiser,
        final Deserialiser deserialiser
    ) {
        this.actorSystem = actorSystem;
        this.peer = Peer$.MODULE$.apply(
                new InetSocketAddress(port),
                DISCOVERY_ADDR,
                this,
                serialiser,
                deserialiser,
                actorSystem
        );
        this.receiver = receiver;
        actorSystem.scheduler().schedule(
            FiniteDuration.Zero(),
            FiniteDuration.apply(10, TimeUnit.SECONDS),
            this::updateStatistics,
            actorSystem.dispatcher()
        );
    }

    public boolean isConnected() {
        return connected.getValue();
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public void connect(final Window parent, final String sessionId) {
        final ProgressDialog progressMonitor = new ProgressDialog(
                parent,
                "Connecting to Peer",
                this::disconnect
        );
        progressMonitor.setVisible(true);

        connectionProgress = Optional.of(progressMonitor);
        peer.tell(Register.apply(sessionId), noSender());
    }

    public void disconnect() {
        connected.set(false);
        peer.tell(Cancel$.MODULE$, noSender());
        peer.tell(Close$.MODULE$, noSender());
    }

    public void updateStatistics() {
        if (connected.get()) {
            final Future<Object> finished = akka.pattern.Patterns.ask(peer, GetStatistics$.MODULE$, 10000);
            finished.onSuccess(new OnSuccess<Object>() {
                @Override
                public void onSuccess(final Object result) throws Throwable {
                    statistics.set((Statistics) result);
                    timeSinceLastReceived.set(System.currentTimeMillis() - lastReceivedTime);
                }
            }, actorSystem.dispatcher());
        }
    }

    public void send(final Object data) {
        peer.tell(RawData.apply(data), noSender());
    }

    public Future<Terminated> shutdown() {
        return actorSystem.terminate();
    }

    @Override
    public void waitingForPeer() {
        withConnectionDialog(cp -> {
            cp.setMessage("Waiting for peer ...");
        });
    }

    @Override
    public void connectedToPeer() {
        connected.set(true);
        withConnectionDialog(cp -> {
            cp.setMessage("Connected to peer");
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
        ErrorDialog.show("Connection Timeout", "Peer discovery timed out. Try again later.", Optional.empty());
    }

    @Override
    public void discoveryCancelled() {
    }

    private void withConnectionDialog(final Consumer<ProgressDialog> action) {
        connectionProgress.ifPresent(cp -> EventQueue.invokeLater(() -> action.accept(cp)));
    }

    public ObjectProperty<Statistics> statistics() {
        return statistics;
    }

    public LongProperty timeSinceLastReceived() {
        return timeSinceLastReceived;
    }
}
