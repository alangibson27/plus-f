package com.socialthingy.plusf.spectrum.network;

import java.net.InetSocketAddress;

public class SessionInfo {
    private boolean host;
    private InetSocketAddress destination;

    public SessionInfo(final boolean host, final InetSocketAddress destination) {
        this.host = host;
        this.destination = destination;
    }

    public SessionInfo(final String stringRepresentation) {
        final String[] parts = stringRepresentation.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unexpected format of SessionInfo");
        }

        if ("HOST".equals(parts[0])) {
            this.host = true;
        } else if ("GUEST".equals(parts[0])) {
            this.host = false;
        } else {
            throw new IllegalArgumentException("Expected HOST or GUEST as first part of SessionInfo");
        }

        final String[] hostPort = parts[1].split(":");
        if (hostPort.length != 2) {
            throw new IllegalArgumentException("Unexpected format of second part of SessionInfo");
        }
        this.destination = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
    }

    public String toString() {
        return String.format("%s,%s:%d", host ? "HOST" : "GUEST", destination.getHostString(), destination.getPort());
    }

    public boolean isHost() {
        return host;
    }

    public InetSocketAddress getDestination() {
        return destination;
    }
}
