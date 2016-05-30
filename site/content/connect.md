+++
date = "2016-05-24T22:31:58+01:00"
draft = false
title = "Connect"
+++

Playing in two-player mode works by one player running the Plus-F Emulator, and the other one running the Guest.
The player running the Emulator has full control of the keyboard, and sends a copy of the screen to the Guest at
a rate of 25 times a second. Meanwhile, keypresses on the Guest are translated into Kempston joystick movements and
sent back to the Emulator.

### Is it really possible?

Limited amounts of testing on the setup below have shown that it is possible for the Emulator and the Guest to be
responsive enough to one another to make two-player games playable. Clearly, some latency may occur at times, causing
the Guest's copy of the display to jump or jerk a little.

* Emulator running on Windows 10, connected to the internet wirelessly via a non-fibre connection
* Guest running on Ubuntu, connected to the internet wirelessly via a 3G phone acting as a portable hotspot
     
If both Emulator and Guest are on fast wireless or wired connections, or on the same local network, performance
should be acceptable.     

### Who should run the Emulator?

Generally, because the Emulator has to send much more data to the Guest than the Guest does back to the Emulator, the
player with the better network upload speeds should run the Emulator. If both players are connected to the same
router, it probably doesn't matter which is which, but over the Internet this does become significant.

### Firewalls and network connectivity

In order to keep network play as responsive as possible, Plus-F uses direct communication between the Emulator and
the Guest (commonly known as Peer-to-Peer or P2P communication). Depending on your computer and internet service
provider's security settings, you may need to change some firewall rules so that the Emulator and Guest can reach
each other.

Details of how to do this will vary based on the operating system, router and ISP you are using, but you need to ensure
that your computer can receive inbound UDP network traffic on ports 7000 and 7001. If you're already familiar with
enabling multi-player games on your computer, the process you need to follow for Plus-F will be similar to that.

### Making a connection

First, the two players need to tell each other how the Emulator and Computer can reach one another. Each player should
invoke the *Get connection details* item from the *Network* menu, and send the host and port details displayed to the
other player.

The other player then invokes the *Connect to guest/emulator* item from the same menu to make the connection.
Eventually, the Emulator's display should be sent to the Guest, and status messages below the display on both the
Emulator and the Guest will indicate the status and quality of the connection.