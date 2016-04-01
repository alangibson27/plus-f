package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Host {
    private final InetAddress guestAddress;
    private final int guestPort;
    private final DatagramSocket datagramSocket;
    private final Consumer<KeyEvent> keyHandler;

    public Host(
        final int hostPort,
        final InetAddress guestAddress,
        final int guestPort,
        final Consumer<KeyEvent> keyHandler
    ) throws SocketException {
        this.guestAddress = guestAddress;
        this.guestPort = guestPort;
        this.keyHandler = keyHandler;

        this.datagramSocket = new DatagramSocket(hostPort);
        Executors.newSingleThreadExecutor().submit(new GuestDataReceiver());
    }

    public boolean sendToGuest(final ArrayList<KeyCode> allowedKeys, final int[] memory, final int[] borderLines, final boolean flashActive) {
        final byte[] screenBytes = new byte[6912];
        for (int i = 0; i < 6912; i++) {
            screenBytes[i] = (byte) memory[16384 + i];
        }

        final byte[] hostData = SerializationUtils.serialize(new HostData(allowedKeys, screenBytes, borderLines, flashActive));
        final DatagramPacket data = new DatagramPacket(hostData, hostData.length, guestAddress, guestPort);
        try {
            datagramSocket.send(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private class GuestDataReceiver implements Runnable {
        @Override
        public void run() {
            while(true) {
                final DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    datagramSocket.receive(packet);
                    keyHandler.accept(SerializationUtils.deserialize(packet.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
