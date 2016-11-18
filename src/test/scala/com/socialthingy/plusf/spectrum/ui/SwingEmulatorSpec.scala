package com.socialthingy.plusf.spectrum.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import javax.swing.JRadioButtonMenuItem

import com.socialthingy.plusf.spectrum.display.UnsafePixelMapper
import com.socialthingy.plusf.spectrum.ui.DisplayComponent.targetPixelAt
import org.fest.swing.core.KeyPressInfo
import org.fest.swing.core.KeyPressInfo.keyCode
import org.fest.swing.fixture.{FrameFixture, JMenuItemFixture}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest._

import scala.language.implicitConversions

class SwingEmulatorSpec extends FlatSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with Inspectors with TableDrivenPropertyChecks {

  val memory = Array.ofDim[Int](0x10000)
  val display = new SwingDoubleSizeDisplay(new UnsafePixelMapper)
  var emulator: SwingEmulator = null
  var fixture: FrameFixture = null

  override def afterAll(): Unit = {
    emulator.stop()
  }

  before {
    if (emulator == null) {
      emulator = new SwingEmulator(memory, display)
      fixture = new FrameFixture(emulator)
      emulator.run()
      Thread.sleep(1000)
    }
  }

  after {
    fixture.menuItem(s"HostJoystickNone").click()
    emulator.resetComputer()
    Thread.sleep(1000)
  }

  val joystickPermutations = Table(
    ("New Host", "New Guest"),
    ("Sinclair", "Kempston"),
    ("Kempston", "Sinclair")
  )

  "Joystick menus" should "not assign a host joystick when the guest joystick is changed" in {
    // when
    fixture.menuItem("GuestJoystickSinclair").click()

    // then
    fixture.menuItem("HostJoystickNone").selected shouldBe true
  }

  forAll(joystickPermutations) { (newHostJoystick, newGuestJoystick) =>
    it should s"switch the guest joystick to $newGuestJoystick when the host chooses $newHostJoystick" in {
      // given
      fixture.menuItem(s"HostJoystick$newGuestJoystick").click()
      fixture.menuItem(s"GuestJoystick$newHostJoystick").click()

      // when
      fixture.menuItem(s"HostJoystick$newHostJoystick").click()

      // then
      fixture.menuItem(s"GuestJoystick$newHostJoystick").selected shouldBe false
      fixture.menuItem(s"GuestJoystick$newGuestJoystick").selected shouldBe true
    }
  }

  implicit class JMenuItemFixtureOps(item: JMenuItemFixture) {
    def selected: Boolean = item.target match {
      case x: JRadioButtonMenuItem => x.isSelected
      case _ => false
    }
  }

  "Emulator" should "run a simple 48k basic program which changes the border and sets some pixels" in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.typeProgram("B0")
    fixture.typeProgram("O16384,255")

    // then
    forEvery(display.borderPixels) { _ shouldBe 0xff000000 }
    forEvery(0 to 6) { x =>
      display.targetPixels(targetPixelAt(x, 0, 0, 0)) shouldBe 0xff000000
      display.targetPixels(targetPixelAt(x, 0, 0, 1)) shouldBe 0xff000000
      display.targetPixels(targetPixelAt(x, 0, 1, 0)) shouldBe 0xff000000
      display.targetPixels(targetPixelAt(x, 0, 1, 1)) shouldBe 0xff000000
    }
  }

  it should "run a simple 128k basic program" in {
    // given
    fixture.enter128kBasic()

    // when
    fixture.typeProgram("POKE 16384, 255")

    // then
    memory(16384) shouldBe 255
  }

  it should "handle Sinclair joystick input correctly" in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.menuItem("HostJoystickSinclair").click()
    fixture.typeProgram("LB=Q+A+O+P+M")

    // and
    fixture.menuItem("HostJoystickNone").click()
    fixture.typeProgram("O16384,B")

    // then
    memory(16384) shouldBe 30
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
  implicit def stringToProgram(line: String): ProgramMagnet = ProgramMagnet(
    Seq(line.map(_.toUpper match {
      case '+' => VK_ADD
      case x => x.toInt
    }))
  )

  implicit def intToKeyPressInfo(i: Int): KeyPressInfo = keyCode(i)
}
