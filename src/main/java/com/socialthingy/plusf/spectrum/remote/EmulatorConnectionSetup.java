package com.socialthingy.plusf.spectrum.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class EmulatorConnectionSetup extends ConnectionSetup {
    public EmulatorConnectionSetup(
        final DatagramSocket socket,
        final String sessionId
    ) {
        super(socket, sessionId);
    }

    @Override
    protected SocketAddress call() throws ConnectionException {
        initialiseConnection();
        final String initAckData = waitForResponse();

        if (initAckData.equals("OK" + sessionId)) {
            final String connData = waitForGuestAddress();
            if (connData.matches("[\\S]+:\\d+")) {
                final String[] parts = connData.split(":", 2);
                updateMessage(String.format("Connection to guest at %s established", connData));
                return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
            } else {
                throw new ConnectionException("Connection failed. Please try again later.");
            }
        } else if (initAckData.equals("KO" + sessionId)) {
            throw new ConnectionException("Connection rejected. Please check the codename is correct.");
        } else {
            throw new ConnectionException("Connection data invalid. Please try again later.");
        }
    }

    private void initialiseConnection() throws ConnectionException {
        updateMessage("Starting connection ...");
        final byte[] initData = ("INIT" + sessionId).getBytes();
        final DatagramPacket initPacket = new DatagramPacket(initData, initData.length, CONNECTOR_ADDR);
        try {
            socket.send(initPacket);
        } catch (IOException e) {
            throw new ConnectionException("Unable to establish a connection. Please check your network connection.", e);
        }
    }

    private String waitForGuestAddress() throws ConnectionException {
        updateMessage("Connection started, waiting for guest ...");
        final DatagramPacket connPacket = new DatagramPacket(new byte[1024], 1024);
        try {
            socket.receive(connPacket);
        } catch (IOException e) {
            throw new ConnectionException("No connection received from guest. Please ask the guest to check their network connection.", e);
        }

        return new String(connPacket.getData(), connPacket.getOffset(), connPacket.getLength());
    }
}
