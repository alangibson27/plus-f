package com.socialthingy.plusf.spectrum.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._

import org.fest.swing.core.KeyPressInfo
import org.fest.swing.core.KeyPressInfo.keyCode
import org.fest.swing.fixture.FrameFixture
import org.scalatest.{BeforeAndAfter, FlatSpec, Inspectors, Matchers}

import scala.language.implicitConversions

class SwingEmulatorSpec extends FlatSpec with Matchers with BeforeAndAfter with Inspectors {

  val emulator = new SwingEmulator()
  val fixture = new FrameFixture(emulator)
  emulator.run()

  after {
    emulator.resetComputer()
    Thread.sleep(1000)
  }

  "Emulator" should "run a simple 48k basic program" in {
    fixture.enter48kBasic()
    fixture.typeProgram("B0")
    forEvery(emulator.display.borderPixels) { _ shouldBe 0xff000000 }
  }

  it should "run a simple 128k basic program" in {
    fixture.enter128kBasic()
    fixture.typeProgram("BORDER 0")
    forEvery(emulator.display.borderPixels) { _ shouldBe 0xff000000 }
  }

  it should "handle Sinclair joystick input correctly" in {
    fixture.enter128kBasic()
    fixture.menuItem("HostJoystickSinclair").click()
    fixture.typeProgram("LET B=Q+A+P+M")
    fixture.typeProgram(List())
    fixture.typeProgram("LET C=B/O")
    fixture.typeProgram(List())
    fixture.menuItem("HostJoystickNone").click()
    fixture.typeProgram("BORDER C")
    forEvery(emulator.display.borderPixels) { _ shouldBe 0xff00cc00 }
  }

  implicit class EmulatorOps(e: FrameFixture) {
    def enter48kBasic(): Unit = {
      Thread.sleep(2000)
      e.pressAndReleaseKey(KeyEvent.VK_UP)
      e.pressAndReleaseKey(VK_ENTER)
      Thread.sleep(2000)
    }

    def enter128kBasic(): Unit = {
      Thread.sleep(2000)
      e.pressAndReleaseKey(KeyEvent.VK_DOWN)
      e.pressAndReleaseKey(VK_ENTER)
      Thread.sleep(1000)
    }

    def typeProgram(program: ProgramMagnet): Unit = {
      Thread.sleep(500)
      program.lines foreach { line =>
        line foreach { key =>
          e.pressAndReleaseKey(key)
        }
        e.pressAndReleaseKey(VK_ENTER)
      }
      Thread.sleep(500)
    }
  }

  case class ProgramMagnet(lines: Seq[Seq[Int]])
  implicit def keypressesToProgram(line: List[Int]): ProgramMagnet = ProgramMagnet(Seq(line))
  implicit def stringToProgram(line: String): ProgramMagnet = ProgramMagnet(
    Seq(line.map(_.toUpper match {
      case '+' => VK_ADD
      case x => x.toInt
    }))
  )

  implicit def intToKeyPressInfo(i: Int): KeyPressInfo = keyCode(i)
}
