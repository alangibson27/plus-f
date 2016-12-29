+++
date = "2016-05-24T22:31:58+01:00"
draft = false
title = "Connect"
+++

### How to do it

Network play works by player A running the Emulator and player B running the Guest. Both of these are downloaded and
available in the same package. Here's how it works:

1. Players A and B both need to ensure that *port forwarding* is enabled on their router for UDP traffic, on ports 7000
   and 7001.

2. Agree on who should run the Emulator. Generally, the player with the better network upload speeds should do this.

3. Choose a code name and share this by email, instant message, text message or however you like.
   It's not especially important what this code is, and you can use the same one over and over again if you like.
   They are only retained until the connection has been made, at which point the +F Relay Service forgets it again.

4. Players A and B both choose the *Connect* option from the *Network* menu, and enter the code name in the pop-up.

5. After a short while, the pop-up should disappear and the Emulator's screen should be shared with the Guest.

5. The guest can use the keys Q, A, O, P and M as a Kempston joystick controller.

Status messages below the display will show the current connection status. The colour of the message indicates the
overall quality of the connection:

* Green: The connection is good enough for games to be playable.
* Yellow: The connection quality is poor, and delays may be experienced.
* Red: The connection has dropped, either temporarily or permanently.

### Does it really work?

Limited amounts of testing on the setup below have shown that it is possible for the Emulator and the Guest to be
responsive enough to one another to make two-player games playable. Clearly, some latency may occur at times, causing
the Guest's copy of the display to jump or jerk a little.

* Emulator running on Windows 10, connected to the internet wirelessly via a non-fibre connection
* Guest running on Ubuntu, connected to the internet wirelessly via a 3G phone acting as a portable hotspot
     
If both Emulator and Guest are on fast wireless or wired connections, or on the same local network, performance
should be acceptable.

### Firewalls and network connectivity

In order to keep network play as responsive as possible, +F uses direct communication between the Emulator and
the Guest (commonly known as Peer-to-Peer or P2P communication). Depending on your computer and internet service
provider's security settings, you may need to change some firewall rules so that the Emulator and Guest can reach
each other.

Details of how to do this will vary based on the operating system, router and ISP you are using, but you need to ensure
that your computer can receive inbound UDP network traffic on ports 7000 and 7001. If you're already familiar with
enabling multi-player games on your computer, the process you need to follow for +F will be similar to that.
