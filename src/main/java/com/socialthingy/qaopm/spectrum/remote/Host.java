package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Host {
    private final DatagramSocket datagramSocket;
    private final Consumer<KeyEvent> keyHandler;

    private Optional<Locator> guestLocator = Optional.empty();

    public Host(final Consumer<KeyEvent> keyHandler, final int localPort) throws SocketException, UnknownHostException {
        final ExternalAddressDiscoverer discoverer = new ExternalAddressDiscoverer(localPort);
        final Optional<Locator> externalAddressAndPort = discoverer.discoverAddress();
        externalAddressAndPort.ifPresent(eap ->
            System.out.printf("Address: %s, port: %d\n", eap.getAddress().toString(), eap.getPort())
        );

        this.keyHandler = keyHandler;
        this.datagramSocket = new DatagramSocket(localPort);
        Executors.newSingleThreadExecutor().submit(new GuestDataReceiver());
    }

    public void connectToGuest(final String guestAddress, final int guestPort) throws UnknownHostException {
        this.guestLocator = Optional.of(new Locator(guestAddress, guestPort));
    }

    public boolean sendToGuest(final ArrayList<KeyCode> allowedKeys, final int[] memory, final int[] borderLines, final boolean flashActive) {
        guestLocator.ifPresent(addr -> {
            final byte[] screenBytes = new byte[6912];
            for (int i = 0; i < 6912; i++) {
                screenBytes[i] = (byte) memory[16384 + i];
            }

            final byte[] hostData = SerializationUtils.serialize(new HostData(allowedKeys, screenBytes, borderLines, flashActive));
            final DatagramPacket data = new DatagramPacket(hostData, hostData.length, addr.getAddress(), addr.getPort());
            try {
                datagramSocket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    private class GuestDataReceiver implements Runnable {
        @Override
        public void run() {
            while(true) {
                final DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    datagramSocket.receive(packet);
                    if (!guestLocator.isPresent()) {
                        guestLocator = Optional.of(new Locator(packet.getAddress().toString(), packet.getPort()));
                    }
                    keyHandler.accept(SerializationUtils.deserialize(packet.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
