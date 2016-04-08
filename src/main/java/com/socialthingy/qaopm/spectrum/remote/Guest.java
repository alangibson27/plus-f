package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Guest {
    private final DatagramSocket datagramSocket;
    private final Set<KeyCode> allowedKeys = new HashSet<>();
    private final Consumer<HostData> screenUpdater;

    private Optional<Locator> hostLocator = Optional.empty();

    public Guest(
        final int localPort,
        final Consumer<HostData> screenUpdater
    ) throws SocketException {
        final ExternalAddressDiscoverer discoverer = new ExternalAddressDiscoverer(localPort);
        final Optional<Locator> externalAddressAndPort = discoverer.discoverAddress();
        externalAddressAndPort.ifPresent(eap ->
            System.out.printf("Address: %s, port: %d\n", eap.getAddress().toString(), eap.getPort())
        );

        this.screenUpdater = screenUpdater;

        this.datagramSocket = new DatagramSocket(localPort);
        Executors.newSingleThreadExecutor().submit(new HostDataReceiver());
        allowedKeys.add(KeyCode.ESCAPE);
        sendKeypress(new KeyEvent(
            KeyEvent.KEY_PRESSED,
            KeyCode.ESCAPE.getName(),
            KeyCode.ESCAPE.getName(),
            KeyCode.ESCAPE,
            false,
            false,
            false,
            false));
    }

    Guest(
        final int guestPort,
        final KeyCode[] allowedKeys,
        final Consumer<HostData> screenUpdater
    ) throws SocketException {
        this(guestPort, screenUpdater);
        for (KeyCode keyCode: allowedKeys) {
            this.allowedKeys.add(keyCode);
        }
    }

    public void connectToHost(final String hostAddress, final int hostPort) throws UnknownHostException {
        this.hostLocator = Optional.of(new Locator(hostAddress, hostPort));
    }

    public boolean sendKeypress(final KeyEvent keyEvent) {
        if (!allowedKeys.contains(keyEvent.getCode()) || !hostLocator.isPresent()) {
            return true;
        }

        try {
            final byte[] bytes = SerializationUtils.serialize(keyEvent);
            final DatagramPacket data = new DatagramPacket(
                bytes,
                bytes.length,
                hostLocator.get().getAddress(),
                hostLocator.get().getPort()
            );
            datagramSocket.send(data);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private class HostDataReceiver implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DatagramPacket data = new DatagramPacket(new byte[16384], 16384);
                try {
                    datagramSocket.receive(data);
                    final HostData hostData = SerializationUtils.deserialize(data.getData());
                    if (hostData.getScreen() != null) {
                        screenUpdater.accept(hostData);
                    }

                    if (hostData.getAllowedKeys() != null) {
                        Guest.this.allowedKeys.clear();
                        Guest.this.allowedKeys.addAll(hostData.getAllowedKeys());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
