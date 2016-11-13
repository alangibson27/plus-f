+++
draft = false
date = "2016-11-04T20:54:07Z"
title = "Standing on the Shoulders of Giants"
tags = ["emulators"]
+++

The fundamental thing an emulator does is, by definition, copy the behaviour of something else. And so it is
with +F, which emulates a real ZX Spectrum. The catch, sadly, is that I don't have one of those to hand, so
without the real thing as a reference, how can I check that +F is working properly? The answer lies in the
fantastic emulators that others have written.<!--more-->

I mainly run Linux at home, and over the years I've enjoyed using the [FUSE Emulator](http://fuse-emulator.sourceforge.net/).
From my time using it, I've developed confidence that it is a faithful emulation of a ZX Spectrum, and at
various points in the development of +F I've used it as my reference. When I wasn't sure if my Z80 emulation
code was setting flags correctly, or I wanted to see what the behaviour of a particular block of assembly code
was, I would run identical tests against FUSE and +F, and where +F gave a different answer, I could be confident
that there was a bug in there which I needed to fix.

On top of that, FUSE also comes with a handy set of debugging tools, which I also made use of at various times -
notably in checking I had the right behaviour for incrementing the value of the refresh register. (There was also
another time where I poured away days of effort trying to squash a bug I'd introduced unthinkingly as a
result of the fundamental misconception that the Spectrum ROM would never try to overwrite itself, but more
on that later.)

All of this is simply to say that, without FUSE, I wouldn't have been able to develop +F. While +F does have
a few tweaks and twiddles that FUSE doesn't have, it's still inferior in many ways, and if you're looking for a
solid Spectrum emulator that's packed full of features and gives you an authentic experience, try FUSE.

It also goes without saying that I'm deeply thankful to FUSE's author for his work, and also very much in awe
of it.
