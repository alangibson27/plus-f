package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Callbacks;
import com.socialthingy.plusf.spectrum.Settings;
import com.socialthingy.plusf.spectrum.UserPreferences;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class GuestPeerAdapter extends PeerAdapter<EmulatorState> implements Callbacks {
    public GuestPeerAdapter(final Consumer<EmulatorState> receiver) {
        super(receiver, Settings.GUEST_PORT, new GuestStateHandler(), new EmulatorStateHandler());
    }

    @Override
    public void connectedToPeer(final InetSocketAddress address) {
        super.connectedToPeer(address);
        new UserPreferences().set(UserPreferences.PREVIOUS_SESSION, new SessionInfo(false, address).toString());
    }
}
