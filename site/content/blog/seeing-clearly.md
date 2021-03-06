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
(for those of you too young to remember, it was sort of like the best bits of FIFA and PES combined in a single game,
only about a hundred times better than that) and we were at the time convinced we would be world champions at it,
if such a competition had existed. (Sadly, eSports wasn't to come into being until a good 10 years after we were past
our gaming peak, and the world had mistakenly moved on from Match Day II by then anyway.)

I can't remember who, but one or the other of us signed off by saying *I'm sure I could still hammer you*, and
it put me to thinking ... which one of us would actually win? The simple way to solve it would be to get together
in front of a computer, fire up an emulator (FUSE, for example) and sort it out. Sadly, at the time, we were
on almost exactly opposite sides of the world, and it would have been a long way for either of us to fly simply
to settle an old childhood rivalry.

Nonetheless, it planted an idea in my mind. Given how easy it is to make high quality video calls to faraway
places, surely there must be no problem in sending Spectrum screenshots in one direction and joystick movements
in another, at least in theory? Depending on the distance and quality of network connection, things might chug
along a little slowly at times, but the Spectrum was never known as a speed machine, was it?

I toyed with the idea for a while before I started any code. My first thought was whether I could bolt some
network play facilities onto an existing open-source emulator, most of which tend to be written in C or some
similar low-level language. I rejected this idea primarily because I thought trying to understand someone
else's C code would be beyond me, but also because I wanted the emulator to be usable across Linux and
Windows, and a language with simpler cross-platform support would be a better option.

So that more or less put me in the position of writing my own emulator from the ground up. It's a challenge I
did relish, but I wasn't in any illusion about the amount of work it would need. Where to start?

More importantly, where to *stop*?

I only wanted to be able to play a couple of games of Match Day II, so there wouldn't be any need to go overboard
with whizzbang features that made no difference to the quality of gameplay. Those thoughts left me with a very clear
vision of what I wanted +F to be:

* An emulator that worked on (at least) Linux and Windows.
* Network play capability, with a fully-fledged emulator at one end and a "dumb" console at the other.
* 48k model only.
* Allows a full game of Match Day II to be played without crashing, even if nothing else works.
* No sound.
* No loading borders.
* No superfluous UI features or twiddles.

And there it was, the minimum viable product for +F. I didn't go as far as setting up a backlog for it or
anything like that, but armed with a clear idea of what I wanted, I was able to decide where to start, what to do
next, and what not to bother with. That's really important, because even in something with a scope that seems
limited, like a Spectrum emulator, there are a lot more things going on than you might expect, and a lot of
places you could choose to put your effort.

Take borders. It's possible to change the colour of the border at specific positions down the screen, and
indeed games like [Aquaplane](http://www.worldofspectrum.org/infoseekid.cgi?id=0000227) use this to create a
"horizon" effect that spans the full width of the screen. However, making the change at the correct position
is intimately linked with the timing of the Spectrum's display hardware. The rules for emulating this correctly
are [very complicated](http://www.worldofspectrum.org/faq/reference/48kreference.htm#ZXSpectrum) and would take
an age to implement and test.

For my purposes, though, because I had already established Match Day II was my priority, and because it
doesn't do anything at all the border (it's black the whole time) it was an easy choice for me not to care
about this at all. This sort of focus that I had because of my clear vision of +F saved me from
disappearing off into the more arcane (but no less technically interesting) aspects of building an emulator.

How does any of this relate to what you and I do in our day jobs? Well, imagine you're maintaining a library
that's going to be used by some other groups - maybe internal, maybe external, maybe a combination of both.
Once your code has been released into the wild for the first time, you're bound to get people coming to you and
asking for new features. Chances are they'll all request different things - this team wants something that
makes it easier to integrate with Spring, that team wants support for a particular metrics framework, and that
team over there wants a version that produces XML rather than JSON.

Reasonable though all these requests may be, if you say *yes* to all of them, you'll end up with a bloated
library, it'll be more involved to maintain and release, client teams will have a tougher time to upgrade,
and in the end every encounter with your library will become a chore rather than a pleasure. You'll also run
the risk of losing focus on the core capabilities of your library, and you'll spend time and effort on things
that only a fraction of your user base will use, rather than using that time to make your code more robust,
correct, efficient or easy to integrate with.

In the end, it comes down to one thing - do you have a clear idea of what the core purpose of your library is?
Sure, you could add some code that will produce an XML form of your output, and the people who asked for it
will doubtless be thankful (today, anyway). But should your library really be getting involved in those sort of
serialisation issues - maybe it should be producing a logical model and leaving it up to the teams who use it
to decide how its output appears instead?

Maybe adding the XML support (or whatever) *is* absolutely the right thing to do, but that's a question only
you (and the rest of your team) can answer. If you're given a requirement and you can't answer the question
*Do we need this?* with a straight *yes* or *no*, it's well worth your time to figure out why you can't.
