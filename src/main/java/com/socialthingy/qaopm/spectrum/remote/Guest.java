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

public class Guest {
    private final InetAddress hostAddress;
    private final int hostPort;
    private final DatagramSocket datagramSocket;
    private final Set<KeyCode> allowedKeys = new HashSet<>();

    public Guest(final int guestPort, final InetAddress hostAddress, final int hostPort) throws SocketException {
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;

        this.datagramSocket = new DatagramSocket(guestPort);
        Executors.newSingleThreadExecutor().submit(new AllowedKeysReceiver());
    }

    Guest(final int guestPort, final InetAddress hostAddress, final int hostPort, final KeyCode[] allowedKeys) throws SocketException {
        this(guestPort, hostAddress, hostPort);
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

    private class AllowedKeysReceiver implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DatagramPacket data = new DatagramPacket(new byte[1024], 1024);
                try {
                    datagramSocket.receive(data);
                    Guest.this.allowedKeys.clear();
                    Guest.this.allowedKeys.addAll(SerializationUtils.deserialize(data.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
