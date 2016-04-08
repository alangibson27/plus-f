package com.socialthingy.qaopm.spectrum.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Locator {
    private final InetAddress address;
    private final int port;

    public Locator(final String name, final int port) throws UnknownHostException {
        this.address = InetAddress.getByName(name);
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
