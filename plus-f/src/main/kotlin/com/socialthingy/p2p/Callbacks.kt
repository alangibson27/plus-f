package com.socialthingy.p2p

import java.net.InetSocketAddress

interface Callbacks {
    fun data(content: Any)
    fun discoveryTimeout()
    fun discoveryCancelled()
    fun waitingForPeer()
    fun connectedToPeer(address: InetSocketAddress)
    fun discovering()
    fun initialising()
    fun closed()
}
