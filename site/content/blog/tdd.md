+++
title = "If You Only Do One Thing"
draft = false
date = "2016-11-28T23:25:34Z"
tags = ["software development", "testing"]
+++
Over the two decades that I've been in software, there have been all number of innovations, techniques, tools,
tricks and plain fads which purport to help you build applications better - model-driven code generation, unit testing,
documenting everything, documenting nothing, pair programming, SOAP, REST, microservice architectures to name a few.
I can understand why most of them (with the exception of SOAP, which is an abomination) could be thought to deliver
some sort of benefit, but for me there's one which stands above all the others: Test-Driven Development.

In honesty, I came to the TDD party pretty late. Of course, I'd known about unit testing and automated testing for a
long time, but I'd never seen it actually work. What tended to happen was that a conscientious newcomer to the team
would write some tests for existing code, and then they'd be maintained for some period of time before falling into
(for want of a better word) disrepair, spending as much time failing as passing, and eventually being deleted or ignored.
When I first came into a TDD environment it opened my eyes to how to do automated testing effectively, and when I look
back to the Before-TDD era, I see nothing but missed opportunities to do things better.

I've done TDD with JUnit, ScalaTest, PyTest, Nose and Cucumber, and it works well with any and all of them. It's not
my place to tell you which tools you should use - just pick one that you like and run with it. Even if you're still
writing programs in COBOL for mainframes, there are options out there. TDD is the important bit, not the tools.

I don't think I would have attempted Plus-F if I didn't know about TDD. A processor is basically a large collection of
small behaviours which, although individually simple, combine together into a single ball of near-indecipherable state.
The only way to tackle it was one operation at a time, verifying each one did what the [Z80 User Manual](http://www.z80.info/zip/z80cpu_um.pdf)
said it would, and moving on to the next. Dull, yes, but also entirely necessary. Being (at its heart) a program that
runs programs, I shuddered at the thought of having to debug problems in games (specifically Match Day II, of course)
without having some confidence that the individual processor operations themselves were doing what they should. As it
is, having taken the TDD approach, the major problems that I've found (there are plenty of minor problems but I don't
care so much about them) have been reasonably obvious and easy to debug.

By now, you may have read about the [thwarted attempt to write Plus-F in Python](../fundamentals). I'll confess that
the thought of having to start again from scratch did make me think twice about what I was doing, but what pushed me on
was that I had one thing in my favour - a good set of tests for all of the processor emulation. And, given that I hadn't
got much beyond that part, that meant I had a good set of tests for more or less everything that I needed to rewrite.
Admittedly, those tests were in [Nose in Python](https://nose.readthedocs.io/en/latest/), but picking up on something
else I'd learned over the last year or two, a typical test looked like this:

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

And in doing so, I had a full set of tests available from the outset against which I could code the Java version of
Plus-F. I won't lie by saying that the process of porting to Java was a mere operation in handle-turning, but neither
was it complicated. (It was boring.)

So where's the advertised gaffe that I normally end my articles with?

Well, processor emulation isn't the only complex thing I had to tackle when writing Plus-F. It turns out that, for all
that it was slow and infuriating, and that I spent a large part of my formative years dreading seeing the message
`R Tape loading error, 0:1`, it's actually pretty fiddly to supply the correct input to the Spectrum's tape loading
routines. The main supported format is TZX, which is [remarkably well documented](http://www.worldofspectrum.org/TZXformat.html),
but as anyone who has read a few specs knows, what looks straightforward on paper isn't always easy to turn into working
code.

This is exactly the sort of thing that TDD was made for. However, by this stage, I had a lot of Plus-F working and I'd
become a bit cavalier in my attitude again. (My unfortunate dalliance with Python was by this stage some six months
prior. How quickly we forget.) Certain that I was only a few commits away from a fully-working application, I pinned
my ears back and frenziedly wrote the TZX-handling routines with only a smattering of tests. Most of it worked, granted,
but it was a laughably inefficient (the screen refresh rate slowed noticeably when a tape was being played) first stab
at it. It desperately needed knocking into shape, as do many first attempts.

And that's where the problem began. Inefficient as it may have been, the first TZX routine did actually work acceptably
well. But because I didn't have a thorough set of tests which allowed me to make changes and still verify that each bit
worked, any time I did something that made it run faster, I also introduced a bug somewhere else. In a bit to stop the
endless to-and-fro of fixing one bug and introducing another, I resorted to cobbling together a "regression test" based
on [this class from the original, inefficient, implementation](https://github.com/alangibson27/plus-f/blob/master/plus-f/src/test/java/com/socialthingy/plusf/tape/ReferenceVariableSpeedBlock.java)
and testing all of my fixes with respect to that.

It was a large enough piece of wallpaper to cover the many cracks, but has no other redeeming features to speak of.

Thankfully, I haven't had to touch the TZX-handling routines much of late, but I dread the day that I have to. I think
it's pretty clear to see, when comparing [its code](https://github.com/alangibson27/plus-f/tree/master/plus-f/src/main/java/com/socialthingy/plusf/tape)
with the [Z80 emulation code that was developed with a TDD approach](https://github.com/alangibson27/plus-f/tree/master/plus-f/src/main/java/com/socialthingy/plusf/z80),
that not building it by TDD has caused a lot of problems there needn't have been.

The lesson? It's all too easy to consider brushing TDD aside in an effort to do something faster, but it's just cutting
a corner, no matter how much you try to rationalise it. Code is usually more complex than you think it will be, you will
need to go back and fix things, and the time you don't spend up-front writing tests is multiplied times over after the
fact.