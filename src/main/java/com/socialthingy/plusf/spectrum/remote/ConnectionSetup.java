package com.socialthingy.plusf.spectrum.remote;

import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.socialthingy.plusf.spectrum.Settings.*;

abstract class ConnectionSetup extends Task<SocketAddress> {
    protected static final SocketAddress CONNECTOR_ADDR = new InetSocketAddress(DISCOVERY_HOST, DISCOVERY_PORT);
    protected final DatagramSocket socket;
    protected final String sessionId;

    ConnectionSetup(final DatagramSocket socket, final String sessionId) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    protected String waitForResponse() throws ConnectionException {
        final DatagramPacket joinAckPacket = new DatagramPacket(new byte[1024], 1024);
        try {
            socket.receive(joinAckPacket);
        } catch (IOException e) {
            throw new ConnectionException("Unable to establish a connection. Please check your network connection.", e);
        }
        return new String(joinAckPacket.getData(), joinAckPacket.getOffset(), joinAckPacket.getLength());
    }
}
