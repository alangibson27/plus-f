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

The Emulator and the Guest connect to one another through the Plus-F Relay Service, which allows them to discover one
another's addresses. Follow these steps to get up and running:

1. The player running the Emulator invokes the *Connect to guest* item and enters a connection code.
   It's not especially important what this code is, and you can use the same one over and over again if you like.
   They are only retained until the connection has been made, at which point the Plus-F Relay Service forgets it again.

2. The player running the Emulator passes the connection code to the player running the Guest. This could be by
   email, instant message, phone, shouting across a room - whatever works.

3. The player running the Guest invokes the *Connect to emulator* item, and enters the connection code.

If the two codes match, you should now be enjoying a two-player session.

First, the two players need to tell each other how the Emulator and Computer can reach one another. Each player should
invoke the *Get connection details* item from the *Network* menu, and send the host and port details displayed to the
other player.

Status messages below the display will show the current connection status. The colour of the message indicates the
overall quality of the connection:

* Green: The connection is good enough for games to be playable.
* Yellow: The connection quality is poor, and delays may be experienced.
* Red: The connection has dropped, either temporarily or permanently.
