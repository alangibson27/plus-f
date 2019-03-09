package com.socialthingy.plusf.spectrum.network;

import com.socialthingy.p2p.Callbacks;
import com.socialthingy.plusf.spectrum.Settings;
import com.socialthingy.plusf.spectrum.UserPreferences;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class EmulatorPeerAdapter extends PeerAdapter<GuestState> implements Callbacks {
    public EmulatorPeerAdapter(final Consumer<GuestState> receiver) {
        super(receiver, Settings.COMPUTER_PORT, new EmulatorStateHandler(), new GuestStateHandler());
    }

    @Override
    public void connectedToPeer(final InetSocketAddress address) {
        super.connectedToPeer(address);
        new UserPreferences().set(UserPreferences.PREVIOUS_SESSION, new SessionInfo(true, address).toString());
    }
}
