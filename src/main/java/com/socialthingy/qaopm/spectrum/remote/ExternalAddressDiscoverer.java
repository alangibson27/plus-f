package com.socialthingy.qaopm.spectrum.remote;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

public class ExternalAddressDiscoverer {
    private static final String STUN_SERVER = "jstun.javawi.de";
    private static final int STUN_PORT = 3478;

    private final int localPort;

    public ExternalAddressDiscoverer(final int localPort) {
        this.localPort = localPort;
    }

    public Optional<Locator> discoverAddress() {
        return discoverAddressFromSource();
    }

    private Optional<Locator> discoverAddressFromSource() {
        try (final DatagramSocket testSocket = new DatagramSocket(localPort)) {
            testSocket.setReuseAddress(true);
            testSocket.setSoTimeout(10000);

            final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
            sendMH.generateTransactionID();

            final ChangeRequest changeRequest = new ChangeRequest();
            sendMH.addMessageAttribute(changeRequest);

            final byte[] data = sendMH.getBytes();
            final DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(STUN_SERVER), STUN_PORT);
            testSocket.send(send);

            MessageHeader receiveMH = new MessageHeader();
            while (!(receiveMH.equalTransactionID(sendMH))) {
                DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                testSocket.receive(receive);
                receiveMH = MessageHeader.parseHeader(receive.getData());
                receiveMH.parseAttributes(receive.getData());
            }

            final MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
            return Optional.of(new Locator(ma.getAddress().toString(), ma.getPort()));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
