+++
title = "The Fundamentals are Important"
draft = false
date = "2016-11-13T22:43:30Z"
tags = ["software development", "performance"]
+++

The relationship between a programmer and a language is much like the relationship between the cricketer and a
bat. Unless you know how to handle it properly, you're not going to achieve much and you could end up in a lot of pain.
And so it was with +F, or at least the program which eventually became +F. It started off with the working title
*QAOPM*, and was written in Python.

The choice of language was driven by a couple of things.

First, I aimed to write a cross-platform emulator. Python is readily available on both of my target platforms (Linux and
Windows), and I planned to use the Pygame library to build the user interface. Second, I love Python's
[principles](https://www.python.org/dev/peps/pep-0020/), its elegance and its clarity. (I know the fact that whitespace
is syntactically relevant bothers some people, but it looks natural to me, and it's never tripped me up.) My
opportunities to work with this language had been limited to a few utilities and the odd simple web application,
and I relished the chance to learn more on a project that was a bit more meaty.

That decision made, I set about writing +F, and four enjoyable months later (well, as enjoyable as jumping armpits-deep
into the Z80 reference manual can be) I had a basic working version which could load a program from a snapshot file,
emulate the Z80 and render the Spectrum display. It couldn't accept keyboard input so you couldn't actually do much with
it, but I was sure that would follow in short order. All the unit tests worked, the display rendering was accurate,
and it was time to fire it all up together for the first time and give it a shakedown.

And ... it was slow.

Not a little sluggish, not jerky from time to time, but truly, painfully slow. Right from the outset, I'd been sure
that a modern PC (even my 2010-vintage laptop) would be able to handle emulation of an 8-bit computer without breaking
sweat. After all, there's already an [emulator written in JavaScript](http://torinak.com/qaop) that runs at full speed
in a browser, so while I was expecting I'd need to make a few tweaks here and there, I had just assumed that the emulator
would be fast enough. Not so. What was the problem?

Me.

Not Python. Definitely not Python.

Me.

*(At this point, I want to make perfectly clear that I'm certain that in the hands of an experienced Python programmer,
it is possible to write an emulator with the requisite speed to be usable. If that sounds like you, I've left all of
the code for the [Python +F on GitHub] (https://github.com/alangibson27/qaopm), and in the interests of learning how to
become a better programmer, I'd be very happy if you had the time to take a look and could explain to me how I could
have achieved a better outcome. I'm not having a bash at Python, I'm having a bash at me.)*

I've earned a living for almost two decades writing software in Java, and more recently Scala. I've become to an
extent conditioned to think of software solutions in terms of interactions between objects (although obviously Scala
opens the door to a more functional approach too), and that's how I approached the Python predecessor of +F.

The processor was a class. Each Z80 operation was a class, with related operations existing in a class hierarchy. Memory was
a class. The display was a class. Almost everything was nicely organised into classes, each with short methods which
did strictly one thing - just the way you'd unthinkingly build almost any Java application. For all
that I could read and write Python, though, I'd neglected the fact that an interpreted, dynamically-typed language
behaves differently to a compiled, statically-typed language.

It turns out this was a really big problem for me because my code fell foul of a combination of Python's weak spots (according to
[this page](https://wiki.python.org/moin/PythonSpeed/PerformanceTips#Loops)) - lots of field dereferencing in tight, nested for-loops.
In any other application, I may not have noticed this, but an emulator is basically just one big outer loop, which runs some
other loops inside it on each iteration. The outermost loop runs once for each display refresh cycle (50 times a second
in the case of the Spectrum), with each iteration running the processor's fetch-execute cycle up to about 17000 times,
followed by another loop which sets the colour of each of the 49152 pixels in the display. Lots of for-loops and, in
my implementation, lots of dereferencing of fields on collaborating objects.

Here's a representative example of how the code for a single emulated Z80 operation (`add a, (hl)`) looked, before I
realised this there were going to be speed issues:

```python
class OpAddAHlIndirect(BaseOp):
    def __init__(self, processor, memory):
        BaseOp.__init__(self)
        self.processor = processor
        self.memory = memory

    def execute(self):
        value = self.memory.peek(self.processor.get_16bit_reg('hl'))
        _add_a(self.processor, value, False)

    def t_states(self):
        return 7

    def __str__(self):
        return 'add a, (hl)'

def _add_a(processor, value, carry):
    signed_a = to_signed(processor.main_registers['a'])
    if carry:
        value = (value + 1) & 0xff
    result, half_carry, full_carry = bitwise_add(processor.main_registers['a'], value)
    signed_result = to_signed(result)
    processor.main_registers['a'] = result
    processor.set_condition('s', signed_result < 0)
    processor.set_condition('z', result == 0)
    processor.set_condition('h', half_carry)
    processor.set_condition('p', (signed_a < 0) != (signed_result < 0))
    processor.set_condition('n', False)
    processor.set_condition('c', full_carry)
```

The execute method is the one that is run in the emulation loop. This single method, which could in theory be executed
up to 17000 times every 1/50th of a second, was itself doing three expensive field dereferences per iteration, and when
you include those done by the `_add_a` function it invokes, in total there are about a dozen in executing this one simple
operation!

Having gone back and done the background reading I ought to have done at the start, I spent a month trying to remedy the
problem, the outcome of which was that the code for our representative `add a, (hl)` operation became ...

```python
class OpAddAHlIndirect(BaseOp):
    def __init__(self, processor, memory):
        BaseOp.__init__(self)
        self.processor = processor
        self.memory = memory

    def execute(self, processor, memory, pc):
        value = memory[0xffff & processor.get_16bit_reg('hl')]
        _add_a(processor, value, False)
        return 7, False, pc

    def __str__(self):
        return 'add a, (hl)'

def _add_a(processor, value, carry):
    signed_a = to_signed(processor.main_registers['a'])
    if carry:
        value = (value + 1) & 0xff
    result, half_carry, full_carry = bitwise_add(processor.main_registers['a'], value)
    signed_result = to_signed(result)
    processor.main_registers['a'] = result
    set_condition = processor.set_condition
    set_condition('s', signed_result < 0)
    set_condition('z', result == 0)
    set_condition('h', half_carry)
    set_condition('p', (signed_a < 0) != (signed_result < 0))
    set_condition('n', False)
    set_condition('c', full_carry)
```

... and while reducing the number of dot-dereferences (complemented with a few other recommended techniques such as
flattening nested loops) did help a bit, it wasn't enough to make the emulator fast enough to be viable. Suitably
discouraged, I decided to scrap the idea of writing an emulator in Python and retreated to the safer territory of Java.

Where had I gone wrong?

I had my cricket bat, I'd carefully seasoned it with linseed oil and I'd spent my time in the nets, honing my technique.
I'd then tried to play snooker with it.

I hadn't understood the fundamentals of Python. You most certainly *can* write Java-esque object-oriented programs with
it, but what is cheap in Java comes with a different cost in Python. I fell into a trap
[articulately described by Joel Spolsky](http://www.joelonsoftware.com/articles/fog0000000319.html), where I didn't
see the details beneath an abstraction, and paid the price in terms of poor performance. (If you haven't read that
article yet, stop right now, read and understand it, read it again, then read some of the other articles on Joel on Software
before coming back here. It'll be well worth your while, trust me.)

I hope I won't make this particular mistake again, but how could I have avoided this problem? I could have read some
information about the performance characteristics of Python in advance of starting, of course. But given that I'm not
especially taken with the idea of lots of up-front background reading when there's code I could be writing (this may or
may not be a character flaw, I leave that for you to judge), I think the best thing I could have done is reached a point
where I realised my mistake a lot sooner.

Premature optimisation is of course a problem in itself (more on *that* later), but a spot of up-front prototyping would have
definitely helped. For example, I could have built a representative subset of the Z80 instruction set and performance
tested a few simple routines, or built the display rendering logic and tested it standalone at the required frame rate.
I'm sure that either of these would have made me either change the way I was writing my Python, or make the jump to
Java sooner than I did.

In honesty, it would have been easy to give up once I realised my best chance was to start all over again, but I did
have one thing in my favour which was going to ease the pain considerably. I'll tell you about that next time.
