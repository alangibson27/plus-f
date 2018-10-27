package com.socialthingy.plusf.spectrum.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import javax.swing.JRadioButtonMenuItem

import com.socialthingy.plusf.spectrum.UserPreferences
import com.socialthingy.plusf.spectrum.UserPreferences.MODEL
import com.socialthingy.plusf.spectrum.display.PixelMapper.SCREEN_WIDTH
import com.socialthingy.plusf.spectrum.display.Scaler2X.SCALE
import com.socialthingy.plusf.spectrum.display.{DisplayComponent, PixelMapper}
import org.fest.swing.core.KeyPressInfo
import org.fest.swing.core.KeyPressInfo.keyCode
import org.fest.swing.fixture.{FrameFixture, JMenuItemFixture}
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.language.implicitConversions

object UITest extends Tag("UITest")

class SwingEmulatorSpec extends FlatSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with Inspectors with TableDrivenPropertyChecks {

  val display = new TestDisplayComponent(new PixelMapper)
  var emulator: InspectableEmulator = null
  var fixture: FrameFixture = null
  val prefs = {
    val p = new UserPreferences()
    p.set(MODEL, "PLUS_2")
    p
  }

  override def afterAll(): Unit = {
    emulator.stop()
  }

  before {
    if (emulator == null) {
      emulator = new InspectableEmulator(prefs, display)
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

  "Joystick menus" should "not assign a host joystick when the guest joystick is changed" taggedAs(UITest) in {
    // when
    fixture.menuItem("GuestJoystickSinclair").click()

    // then
    fixture.menuItem("HostJoystickNone").selected shouldBe true
  }

  forAll(joystickPermutations) { (newHostJoystick, newGuestJoystick) =>
    it should s"switch the guest joystick to $newGuestJoystick when the host chooses $newHostJoystick" taggedAs(UITest) in {
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

  "Emulator" should "run a simple 48k basic program which changes the border and sets some pixels" taggedAs(UITest) in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.typeProgram("B0")
    fixture.typeProgram("O16384,255")

    // then
    forEvery(display.getBorderColours) { _ shouldBe 0xff000000 }
    forEvery(0 to 6) { x =>
      display.getTargetPixels(targetPixelAt(x, 0, 0, 0)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 0, 1)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 1, 0)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 1, 1)) shouldBe 0xff000000
    }
  }

  it should "run a simple 128k basic program" taggedAs(UITest) in {
    // given
    fixture.enter128kBasic()

    // when
    fixture.typeProgram("POKE 16384, 255")

    // then
    emulator.peek(16384) shouldBe 255
  }

  it should "handle Sinclair joystick input correctly" taggedAs(UITest) in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.menuItem("HostJoystickSinclair").click()
    fixture.typeProgram("LB=Q+A+O+P+M")

    // and
    fixture.menuItem("HostJoystickNone").click()
    fixture.typeProgram("O16384,B")

    // then
    emulator.peek(16384) shouldBe 30
  }

  implicit class EmulatorOps(e: FrameFixture) {
    def enter48kBasic(): Unit = {
      Thread.sleep(3000)
      e.pressAndReleaseKey(KeyEvent.VK_UP)
      e.pressAndReleaseKey(VK_ENTER)
      Thread.sleep(2000)
    }

    def enter128kBasic(): Unit = {
      Thread.sleep(3000)
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

  def targetPixelAt(mainx: Int, mainy: Int, subx: Int, suby: Int) = (mainx * SCALE + subx) + ((mainy * SCALE + suby) * (SCREEN_WIDTH * SCALE))
}

class TestDisplayComponent(mapper: PixelMapper) extends DisplayComponent(mapper) {
  def getBorderColours: Array[Int] = borderImageDataBuffer
  def getTargetPixels: Array[Int] = imageDataBuffer
}

class InspectableEmulator(prefs: UserPreferences, display: DisplayComponent)
  extends Emulator(prefs, display) {
  def peek(addr: Int): Int = computer.peek(addr)
}