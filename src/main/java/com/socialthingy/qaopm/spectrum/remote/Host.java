package com.socialthingy.qaopm.spectrum.remote;

import javafx.scene.input.KeyCode;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Host {
    private final InetAddress guestAddress;
    private final int guestPort;
    private final DatagramSocket datagramSocket;

    public Host(final int hostPort, final InetAddress guestAddress, final int guestPort) throws SocketException {
        this.guestAddress = guestAddress;
        this.guestPort = guestPort;

        this.datagramSocket = new DatagramSocket(hostPort);
    }

    public boolean send(final int[] memory) {
        try {
            final byte[] screenBytes = new byte[6912];
            for (int i = 0; i < 6912; i++) {
                screenBytes[i] = (byte) memory[16384 + i];
            }
            final DatagramPacket data = new DatagramPacket(screenBytes, 6912, guestAddress, guestPort);
            datagramSocket.send(data);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sendAllowedKeys(final ArrayList<KeyCode> allowedKeys) {
        try {
            final byte[] keyBytes = SerializationUtils.serialize(allowedKeys);
            final DatagramPacket data = new DatagramPacket(keyBytes, keyBytes.length, guestAddress, guestPort);
            datagramSocket.send(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
