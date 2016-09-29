package com.socialthingy.plusf.spectrum.network;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.socialthingy.plusf.p2p.*;
import com.socialthingy.plusf.spectrum.dialog.ErrorDialog;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static akka.actor.ActorRef.noSender;
import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_HOST;
import static com.socialthingy.plusf.spectrum.Settings.DISCOVERY_PORT;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

public class PeerAdapter<T> implements Callbacks {
    private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress(DISCOVERY_HOST, DISCOVERY_PORT);
    private static final ActorSystem actorSystem = ActorSystem.apply();

    private final ActorRef peer;
    private final Consumer<T> receiver;
    private Optional<Alert> connectionProgress = Optional.empty();
    private boolean connected;

    private double avgLatency;
    private int outOfOrder;
    private double avgSize;
    private long lastReceivedTime = 0;

    public PeerAdapter(
        final Consumer<T> receiver,
        final int port,
        final Serialiser serialiser,
        final Deserialiser deserialiser
    ) {
        this.peer = Peer$.MODULE$.apply(
                new InetSocketAddress(port),
                DISCOVERY_ADDR,
                this,
                serialiser,
                deserialiser,
                actorSystem
        );
        this.receiver = receiver;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect(final String sessionId) {
        final Alert alert = new Alert(INFORMATION);
        alert.getButtonTypes().setAll(ButtonType.CANCEL);
        alert.setHeaderText("Please wait ...");
        alert.setTitle("Connecting to Peer");
        alert.setGraphic(new ProgressIndicator(-1.0));

        alert.setOnCloseRequest(de -> {
            if (!connected) {
                peer.tell(Close$.MODULE$, noSender());
            }
        });
        alert.show();

        connectionProgress = Optional.of(alert);
        peer.tell(Register.apply(sessionId), noSender());
    }

    public void disconnect() {
        connected = false;
        peer.tell(Close$.MODULE$, noSender());
    }

    public void updateStatistics() {
        final Future<Object> finished = akka.pattern.Patterns.ask(peer, GetStatistics$.MODULE$, 10000);
        try {
            final Statistics result = (Statistics) Await.result(finished, new FiniteDuration(1, TimeUnit.SECONDS));
            avgLatency = result.avgLatency();
            avgSize = result.avgSize();
            outOfOrder = result.outOfOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(final Object data) {
        peer.tell(RawData.apply(data), noSender());
    }

    public void shutdown() {
        actorSystem.terminate();
    }

    @Override
    public void waitingForPeer() {
        withConnectionDialog(cp -> cp.setHeaderText("Waiting for peer ..."));
    }

    @Override
    public void connectedToPeer() {
        connected = true;
        withConnectionDialog(cp -> {
            cp.setHeaderText("Connected to peer");
            cp.close();
            connectionProgress = Optional.empty();
        });
    }

    @Override
    public void discovering() {
        withConnectionDialog(cp -> cp.setHeaderText("Contacting discovery service ..."));
    }

    @Override
    public void initialising() {
        withConnectionDialog(cp -> cp.setHeaderText("Initialising peer ..."));
    }

    @Override
    public void closed() {
        connected = false;
    }

    @Override
    public void data(final Object content) {
        receiver.accept((T) content);
        this.lastReceivedTime = System.currentTimeMillis();
    }

    @Override
    public void discoveryTimeout() {
        withConnectionDialog(Alert::close);
        connectionProgress = Optional.empty();
        ErrorDialog.show("Connection Timeout", "Peer discovery timed out. Try again later.", Optional.empty());
    }

    private void withConnectionDialog(final Consumer<Alert> action) {
        connectionProgress.ifPresent(cp -> Platform.runLater(() -> action.accept(cp)));
    }

    public double getAverageLatency() {
        return avgLatency;
    }

    public int getOutOfOrderPacketCount() {
        return outOfOrder;
    }

    public double getAveragePacketSize() {
        return avgSize;
    }

    public Paint getConnectionHealth() {
        if (avgLatency > 40.0) {
            return Color.ORANGE;
        } else if (System.currentTimeMillis() - lastReceivedTime > 500) {
            return Color.RED;
        } else {
            return Color.GREEN;
        }
    }
}
