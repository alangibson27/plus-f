+++
draft = false
title = "Seeing Clearly"
date = "2016-11-06T22:02:33Z"
tags = ["software development"]
+++

Before I dive into the missteps and mishaps of +F, I'd like to start with one very important thing I did right,
in the very early stages.

The original idea for +F came from the tail end of an IM conversation with an old school friend, reminiscing
about the computer games we used to play when we were young. A particular favourite of ours was Match Day II
(for those of you too young to remember, it was sort of like the best bits of FIFA and PES brought together in a single game,
multiplied a hundred times) and we were at the time convinced we would be world champions at it, if such a thing
existed. (Sadly, eSports wasn't to emerge until a good 10 years after we were past our gaming peak, and the
world had mistakenly moved on from Match Day II by then anyway.)

I can't remember who, but one or the other of us signed off by saying *I'm sure I could still hammer you*, and
it put me to thinking ... which one of us would actually win? The simple way to solve it would be to get together
in front of a computer, fire up an emulator (FUSE, for example) and sort it out. Sadly, at the time, we were
on almost exactly opposite sides of the world, and it would have been a long way for either of us to fly simply
to settle an old childhood rivalry.

Nonetheless, it planted an idea in my mind. Given how easy it is to make high quality video calls to faraway
places, surely there must be no problem in sending a Spectrum screen in one direction, and joystick movements
in another, at least in theory? Depending on the distance and quality of network connection, things might chug
along a little slowly at times, but the Spectrum was never known as a speed machine, was it?

I toyed with the idea for a while before I started any code. My first thought was whether I could bolt some
network play facilities onto an existing open-source emulator, most of which tend to be written in C or some
similar low-level language. I rejected this thought fairly quickly as I'm a Linux user and most of my friends
use Windows, so I'd also have a whole load of cross-platform issues to wrestle with, once I'd got past the trickier
task of understanding somebody else's C.

So that more or less put me in the position of writing my own emulator from the ground up. It's a challenge I
did relish, but I wasn't in any illusion about the amount of work it would need. Where to start? More
importantly, where to *stop*? I only wanted to be able to play a couple of games of Match Day II, so there
wouldn't be any need to go overboard with whizzbang features that made no difference to the quality of gameplay.

Those thoughts left we with a very clear vision of what I wanted +F to be:

* An emulator that worked on (at least) Linux and Windows.
* Network play capability, with a fully-fledged emulator at one end and a "dumb" console at the other.
* 48k model only.
* Allows a full game of Match Day II to be played without crashing, even if nothing else works.
* No sound.
* No loading borders.
* No superfluous UI features or twiddles.

And there it was, the minimum viable product for +F. I didn't go as far as setting up a backlog for it or
anything like that, but I did realise I had a clear idea of what I wanted, and I was able to use that idea
to help me decide where to start, what to do next, and what not to bother with. That's really important,
because even in something with a scope that seems limited, like a Spectrum emulator, there are a lot more things
going on than you might expect, and a lot of places you could choose to put your effort.

Take borders. It's possible to change the colour of the border at specific positions down the screen, and
indeed games like [Aquaplane](http://www.worldofspectrum.org/infoseekid.cgi?id=0000227) use this to create a
"horizon" effect that spans the full width of the screen. However, making the change at the correct position
is intimately linked with the timing of the Spectrum's display hardware. The rules for emulating this correctly
are [very complicated](http://www.worldofspectrum.org/faq/reference/48kreference.htm#ZXSpectrum) and would take
an age to implement and test.

For my purposes, though, because I had already established Match Day II was my priority, and because it
doesn't do anything at all the border (it's black the whole time) it was an easy choice for me not to care
about this at all. This sort of focus that I had because I had a vision of what I wanted +F to be saved me from
disappearing off into the more arcane aspects of building an emulator.

Now, of course there's a difference between me writing +F in my spare time and you working on a team every day
in the face of deadlines, shifting requirements and feature creep. But I think my point stands. If you're
somehow able to establish a clear idea of what you're building is supposed to do, you'll stand a better chance
of directing your efforts in the right places. If you're faced with a piece of work and you can't answer the
question *do we need this?* with a simple *yes* or *no*, it's worth figuring out why not.