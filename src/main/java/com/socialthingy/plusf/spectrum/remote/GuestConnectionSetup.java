package com.socialthingy.plusf.spectrum.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class GuestConnectionSetup extends ConnectionSetup {
    public GuestConnectionSetup(
        final DatagramSocket socket,
        final String sessionId
    ) {
        super(socket, sessionId);
    }

    @Override
    protected SocketAddress call() throws ConnectionException {
        initialiseConnection();
        final String joinAckData = waitForResponse();

        if (joinAckData.matches("[\\S]+:\\d+")) {
            final String[] parts = joinAckData.split(":", 2);
            updateMessage(String.format("Connection to emulator at %s established", joinAckData));
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        } else if (joinAckData.equals("KO" + sessionId)) {
            throw new ConnectionException("Connection rejected. Please check the codename is correct.");
        } else {
            throw new ConnectionException("Connection data invalid. Please try again later.");
        }
    }

    private void initialiseConnection() throws ConnectionException {
        updateMessage("Joining emulator ...");
        final byte[] joinData = ("JOIN" + sessionId).getBytes();
        final DatagramPacket joinPacket = new DatagramPacket(joinData, joinData.length, CONNECTOR_ADDR);
        try {
            socket.send(joinPacket);
        } catch (IOException e) {
            throw new ConnectionException("Unable to establish a connection. Please check your network connection.", e);
        }
    }
}
