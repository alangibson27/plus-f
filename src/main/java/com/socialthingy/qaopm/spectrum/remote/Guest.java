package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Guest {
    private final InetAddress hostAddress;
    private final int hostPort;
    private final DatagramSocket datagramSocket;
    private final Set<KeyCode> allowedKeys = new HashSet<>();
    private final Consumer<HostData> screenUpdater;

    public Guest(
        final int guestPort,
        final InetAddress hostAddress,
        final int hostPort,
        final Consumer<HostData> screenUpdater
    ) throws SocketException {
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.screenUpdater = screenUpdater;

        this.datagramSocket = new DatagramSocket(guestPort);
        Executors.newSingleThreadExecutor().submit(new HostDataReceiver());
    }

    Guest(
        final int guestPort,
        final InetAddress hostAddress,
        final int hostPort,
        final KeyCode[] allowedKeys,
        final Consumer<HostData> screenUpdater
    ) throws SocketException {
        this(guestPort, hostAddress, hostPort, screenUpdater);
        for (KeyCode keyCode: allowedKeys) {
            this.allowedKeys.add(keyCode);
        }
    }

    public boolean sendKeypress(final KeyEvent keyEvent) {
        if (!allowedKeys.contains(keyEvent.getCode())) {
            return true;
        }

        try {
            final byte[] bytes = SerializationUtils.serialize(keyEvent);
            final DatagramPacket data = new DatagramPacket(bytes, bytes.length, hostAddress, hostPort);
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
