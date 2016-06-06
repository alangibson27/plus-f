package com.socialthingy.plusf.spectrum.remote;

import javafx.concurrent.Task;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class EasySpectrumConnector extends Task<SocketAddress> {

    private static final SocketAddress CONNECTOR_ADDR = new InetSocketAddress("services.baskinginthesun.co.uk", 7070);
    private final DatagramSocket socket;
    private final String sessionId;

    public EasySpectrumConnector(
        final DatagramSocket socket,
        final String sessionId
    ) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    @Override
    protected SocketAddress call() throws Exception {
        updateMessage("Starting connection ...");
        final byte[] initData = ("INIT" + sessionId).getBytes();
        final DatagramPacket initPacket = new DatagramPacket(initData, initData.length, CONNECTOR_ADDR);
        socket.send(initPacket);

        final DatagramPacket initAckPacket = new DatagramPacket(new byte[1024], 1024);
        socket.receive(initAckPacket);
        final String initAckData = new String(initAckPacket.getData(), initAckPacket.getOffset(), initAckPacket.getLength());

        if (initAckData.equals("OK" + sessionId)) {
            updateMessage("Connection started, waiting for guest ...");
            final DatagramPacket connPacket = new DatagramPacket(new byte[1024], 1024);
            socket.receive(connPacket);
            final String connData = new String(connPacket.getData(), connPacket.getOffset(), connPacket.getLength());
            if (connData.matches("[\\S]+:\\d+")) {
                final String[] parts = connData.split(":", 2);
                updateMessage(String.format("Connection to guest at %s established", connData));
                return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
            } else {
                throw new IllegalStateException("Connection failed. Please try again later.");
            }
        } else if (initAckData.equals("KO" + sessionId)) {
            throw new IllegalStateException("Connection rejected. Please check the codename is correct.");
        } else {
            throw new IllegalStateException("Connection data invalid. Please try again later.");
        }
    }
}
