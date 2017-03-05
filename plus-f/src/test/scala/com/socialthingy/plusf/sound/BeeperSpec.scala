package com.socialthingy.plusf.sound

import org.scalatest.{FlatSpec, Matchers}

class BeeperSpec extends FlatSpec with Matchers {

  "Beeper" should "return a frequency of 0.5Hz when there are 2 beeps within an interval of 1 second" in {
    val beeper = new Beeper(true)
    beeper.beep(0)
    beeper.beep(1000)

    beeper.play shouldBe 0.5 +- 0.1
  }

  it should "return a frequency of 1Hz when there are 3 beeps in an interval of 1 second" in {
    val beeper = new Beeper(true)
    beeper.beep(0)
    beeper.beep(500)
    beeper.beep(1000)

    beeper.play shouldBe 1.0 +- 0.1
  }

  it should "return a frequency of 3Hz where there are 4 beeps in an interval of 0.5 seconds" in {
    val beeper = new Beeper(true)
    beeper.beep(0)
    beeper.beep(150)
    beeper.beep(300)
    beeper.beep(500)

    beeper.play shouldBe 3.0 +- 0.1
  }

  it should "return a frequency of 0Hz when there are no beeps" in {
    val beeper = new Beeper(true)
    beeper.play shouldBe 0.0
  }

  it should "return a frequency of 50Hz when there are 3 beeps in an interval of 20 milliseconds" in {
    val beeper = new Beeper(true)
    beeper.beep(0)
    beeper.beep(10)
    beeper.beep(20)

    beeper.play shouldBe 50.0 +- 0.1
  }
}
