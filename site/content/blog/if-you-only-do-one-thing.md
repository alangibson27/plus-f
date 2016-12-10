+++
title = "If You Only Do One Thing"
draft = false
date = "2016-11-28T23:25:34Z"
tags = ["software development", "testing"]
+++
Over the years, there have been all number of innovations, techniques, tools,
tricks and fads purported to help you build better-quality applications more quickly - model-driven code generation,
object-relational frameworks, IoC containers, unit testing, Scrum, Kanban, documenting everything, documenting nothing,
pair programming - even industrial quantities of XML (in the case of the abomination that was SOAP). For me, though,
one stands head and shoulders above all the rest, Test-Driven Development. If you only do one thing tomorrow that you
aren't doing today, choose TDD.

I came to the TDD party pretty late. Of course, I'd known all about unit testing for a long time, but
I'd never actually seen it work, in terms of improving the speed or quality of application development. What I'd
generally seen was that a conscientious newcomer to some team would write tests for new code they were working on, plus
maybe a few other tests where they had to fix existing code. Those tests would be maintained for a period of time,
before falling into (for want of a better word) disrepair, spending as much time failing as passing, and eventually
being deleted or ignored. When I first came into a TDD environment, it opened my eyes to how to make proper use of
automated tests, and when I look back at the times before I used TDD, I see nothing but missed opportunities to build
better software.

I've done TDD with JUnit, ScalaTest, PyTest, Nose and Cucumber, and it works well with any and all of them. I don't
think the choice of tools is especially important - just pick the ones that work for you and use them. Even if you're
still writing programs in COBOL for mainframes, I'm sure there are options out there.

I don't think I would have attempted Plus-F if I wasn't already doing TDD in my day job. A processor is basically a
large collection of instructions which all operate on the same global state. Those instructions can then be combined in
an infinite number of permutations, where the state of a single bit in a single register can cause execution to take a
completely different path. Butterflies flap their wings at every step along the way.

I shuddered at the thought of having to debug a problem in some large Z80 program (i.e. Match Day II) without having
confidence that the individual processor operations were working correctly in isolation.

The only way to tackle it was one operation at a time, writing tests based on what the
[Z80 User Manual](http://www.z80.info/zip/z80cpu_um.pdf) specified, writing the code for the operation, then moving on
to the next. Dull, yes, but it's the only way I could see of gaining any traction. As it is, having taken the TDD
approach, the major problems which stopped programs from working outright have been reasonably rare, obvious and easy to
debug and fix.

By now, you may have read about my [valiant but doomed attempt to write Plus-F in Python](../fundamentals). I'll confess that
the thought of having to start again from scratch did make me think twice about what I was doing, but the choice was made
easier by the one thing I had in my favour - a good set of tests for all of the processor emulation. And, given that I
hadn't got much beyond that part (aside from a prototype display that showed just how slow the emulator was), that meant
I had a good set of tests for more or less everything that I needed to rewrite. Admittedly, those tests were in Python's
[Nose framework](https://nose.readthedocs.io/en/latest/), but picking up on something else I'd learned over the previous
year or two, a typical test looked like this one:

```python
def test_and_a_with_other_reg_giving_zero_result(self):
    # given
    self.given_register_contains_value('a', 0b10101010)
    self.given_register_contains_value('b', 0b01010101)

    self.given_next_instruction_is(0xa0)

    # when
    self.processor.execute()

    # then
    self.assert_register('a').equals(0b00000000)
    self.assert_register('b').equals(0b01010101)

    self.assert_flag('s').is_reset()
    self.assert_flag('z').is_set()
    self.assert_flag('h').is_set()
    self.assert_flag('p').is_set()
    self.assert_flag('n').is_reset()
    self.assert_flag('c').is_reset()
```

In other words, it read sort of like a mini-DSL. A bit of judicious search-and-replace meant that I could port tests
like these easily to an equivalent in ScalaTest like this:

```scala
"and <reg>" should "calculate a zero result when a and <reg> have no shared bits" in new Machine {
  // given
  registerContainsValue("a", binary("10101010"))
  registerContainsValue("b", binary("01010101"))

  nextInstructionIs(0xa0)

  // when
  processor.execute()

  // then
  registerValue("a") shouldBe binary("00000000")
  registerValue("b") shouldBe binary("01010101")

  flag("s").value shouldBe false
  flag("z").value shouldBe true
  flag("h").value shouldBe true
  flag("p").value shouldBe true
  flag("n").value shouldBe false
  flag("c").value shouldBe false
  flag("f3").value shouldBe false
  flag("f5").value shouldBe false
}
```

All I then had to do was write the implementation of the DSL (easy enough), and then I had a full set of tests
available from the outset, against which I could code the Java version of Plus-F. Class-by-class, I converted
the tests and then wrote the code to pass them, and in little over a month I was back to the same point I'd reached in
Python without too much effort. Full-on TDD to the rescue!

Of course, it would be out of character if the faint glimmer of success I saw in the distance wasn't subsequently
extinguished by an act of stupidity, laziness, or (in this case) both.

Processor emulation isn't the only complex thing I had to tackle when writing Plus-F. Tape loading was another
tricky area, and emulating it properly is surprisingly fiddly. There's a [remarkably well-documented specification]
(http://www.worldofspectrum.org/TZXformat.html) of the TZX emulated tape format, and in theory all that's required is
to turn the contents of a TZX file into a stream of 0s and 1s which can be fed bit-by-bit into the emulated Spectrum.
The catch is that the timings and high/low state of the input really have to be precise, or the ROM's tape loading
routine doesn't work. Either the input signal won't be recognised at all, or you're faced with the sight of the
`R Tape Loading Error, 0:1` error message that haunted the childhood of me and many others of my generation.

Intricate and nuanced, but well-documented ... this is exactly the sort of thing that TDD was made for. However, by this
stage, I had a lot of Plus-F working and I'd become a bit cavalier in my attitude again. (My unfortunate dalliance with
Python was by this stage some six months in the past. How quickly the mind forgets.) Certain that I was only a few commits
away from a fully-working application, I pinned my ears back and frenziedly wrote the TZX-handling routines, supported by
only a smattering of tests.

Surprisingly, the code worked. Sadly, however, it was laughably inefficient. When reading from a TZX file, the screen
refresh rate would slow noticeably, and I'm sure all the lights in the house became dimmer while it was running too. It's
hardly unheard of first implementations to be lacking performance-wise, so I could treat it as a proof of concept and
rewrite it piece-by-piece so that it skipped along more efficiently.

That's where the problems *really* began. Inefficient as it may have been, the first TZX routine was fairly good functionally.
But because I didn't have a thorough set of tests which allowed me to make changes and still verify that each part worked,
any time I did something that made it run faster, I also introduced a bug somewhere else. In a bid to stop the incessant
cycle of fixing one bug only to introduce another, I resorted to cobbling together a "regression test" based
on [this class from the original, inefficient, implementation](https://github.com/alangibson27/plus-f/blob/master/plus-f/src/test/java/com/socialthingy/plusf/tape/ReferenceVariableSpeedBlock.java)
and testing all of my potential improvements with respect to that.

It helped, but was nothing more than a length of wallpaper to cover up the cracks in my approach. It's still there.

Thankfully, I haven't had to touch the TZX-handling routines much of late, but I dread the day that I have to. I think
it's pretty clear to see, when comparing [its code](https://github.com/alangibson27/plus-f/tree/master/plus-f/src/main/java/com/socialthingy/plusf/tape)
with the [Z80 emulation code that was developed with a TDD approach](https://github.com/alangibson27/plus-f/tree/master/plus-f/src/main/java/com/socialthingy/plusf/z80),
that not building it by TDD has caused a lot of problems there needn't have been.

The lesson? It's all too easy to consider brushing TDD aside in an effort to get to where you want to be more quickly,
but no matter how much you try to rationalise it, it's still just cutting corners. You can't just wave away the inherent
complexity of software when it suits, but you can certainly use TDD to tame it a bit.
