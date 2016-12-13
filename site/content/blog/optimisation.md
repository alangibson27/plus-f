+++
title = "Going Too Quickly, Working Too Hard"
draft = false
date = "2016-12-13T21:24:44Z"
tags = ["software development", "performance"]
+++
The just-about-holding-together addition of tape loading emulation I told you about
[last time](../if-you-only-do-one-thing) was the last but one of the major bits
of Plus-F. Having reached this milestone, I decided to take some time out and enjoy
playing a game or two of Match Day II before doing any more work on it. My work
may have stopped, but it turns out my computer was working as hard as ever.<!--more-->

The user experience was fairly good, at first glance, with the emulation running
without crashes and at full speed. Indeed, I'd even had time to add a "turbo" mode
which would run the emulation as fast as possible, designed to remove the boredom
of waiting for tapes to load (although this does give a somewhat less authentic
experience and you should hang your head in shame if you do use it). The problem
was that when I had Plus-F running for more than a couple of minutes, the fan on
my laptop would whoosh up to full speed, and stay there more or less until Plus-F
stopped. A quick check with `top` confirmed that Plus-F was using a whole core of
the laptop's dual-core processor.

By any measure, that's too much for a Spectrum emulator running on modern hardware.
The [FUSE](../standing-on-the-shoulders-of-giants) emulator written in C uses only
a few percent of the available CPU capacity on the same computer and OS, and while
I wasn't expecting to achieve that level of performance from a Java application
(a deliberate trade-off of raw performance for fewer cross-platform difficulties),
I was still surprised it was quite so processor-hungry. But why?

I'll spare you the blow-by-blow details, except to say that a few profiling
sessions using [honest-profiler](https://github.com/RichardWarburton/honest-profiler/wiki)
showed that the problem lay in the rendering of the display. The antiquated display
hardware of the Spectrum was efficient both in terms of memory and execution time
(while also giving us the famous [attribute clash](http://speccyholic.tumblr.com/post/89194660510/the-spectrums-secret-weapon))
but does make it slightly convoluted to generate a pixel-based RGB bitmap. Somewhere
in that display emulation were a few methods in particular that appeared to be hogging
the CPU unexpectedly. The surprising thing is that these methods did little else
apart from looking up various mappings between Spectrum display co-ordinates and
the corresponding window co-ordinates. Thinking ahead, I'd decided to pre-compute
these calculations and store the results in an array, since they'd be needed on
every single display refresh, so I couldn't see why these would be slow.

And there was my mistake.

I'd assumed that array lookups are less expensive than repeated method calls, but
that wasn't actually the case for the calculations that I had precomputed. When
you include bounds checking and whatever else is involved in array access, it
actually turned out to be *more* expensive than recalculating the co-ordinates on
every call. In trying to outsmart the JVM with some optimisations of my own, I'd
actually worked against it and stopped it from doing its own, more effective, optimisations
to the code.

For the JVM is very clever indeed. (It does this sort of thing all day, every day, so
it ought to be an expert, I suppose.) Based on the methods it sees being executed
as your program runs, it can choose to compile and inline them as it sees fit. Inlining
in particular tends to work best where methods are short, with the upshot that
[adding method calls can make your code run faster](https://techblug.wordpress.com/2013/08/19/java-jit-compiler-inlining/).

Of course, when it comes to Java code, you could argue that you're better to start
off with code that composes lots of calls to small methods anyway, and you'd be
right. If nothing else, it means that your code is easier to reason about and test,
but as an added bonus you're also staying out of the JVM's way and letting it do
what it's best at.

The mistake I made is one which has been known for a long time, and which [Knuth](https://shreevatsa.wordpress.com/2008/05/16/premature-optimization-is-the-root-of-all-evil/)
popularised with his statement *premature optimisation is the root of all evil*.
Software may have moved on in many ways since he said that in 1974, but to me the
problem of premature optimisation is more human than technological, and I'd be suprised
if we were that much less susceptible to it now than we were then. In my case,
with no evidence to support me, I assumed that certain operations were going to
be costly, and wrote bad code based on that faulty assumption.

How could I have avoided this?

It's a tricky balance. On the one hand, in my original Python implementation of
Plus-F, by doing no profiling or performance testing until the very end, [I ended
up wasting time on an implementation that would never have been fit for purpose]
(../fundamentals). Clearly, I could have done with paying a bit more attention to
performance up-front in that case. In the case of the Java version of Plus-F, by
fixating on performance too early (probably a consequence of having been burned
a little by the fact the first attempt at Plus-F was so slow), I ended up with an
inefficient application.

I think it all boils down to gathering evidence and acting upon that. Where you
have requirements that a program should exhibit a certain level of performance
(in my case not needing an entire CPU core in order to emulate a computerised beermat
from the 1980s), it's important to test for this, just like you would test whether
it behaves correctly or not, and to bake this testing into your development cycle
if you can.
