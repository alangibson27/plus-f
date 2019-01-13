package com.socialthingy.plusf.spectrum.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._

import javax.swing.{JFrame, JRadioButtonMenuItem}
import com.socialthingy.plusf.spectrum.{Model, UserPreferences}
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

  val display = new TestDisplayComponent()
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
      val frame = new JFrame()
      emulator = new InspectableEmulator(frame, prefs, display)

      frame.setJMenuBar(emulator.getMenuBar)
      frame.addKeyListener(emulator.getKeyListener)
      frame.getContentPane.add(emulator)
      fixture = new FrameFixture(frame)
      fixture.show()
      emulator.run()
      Thread.sleep(1000)
    }
  }

  after {
    emulator.reset()
    Thread.sleep(1000)
  }

  implicit class JMenuItemFixtureOps(item: JMenuItemFixture) {
    def selected: Boolean = item.target match {
      case x: JRadioButtonMenuItem => x.isSelected
      case _ => false
    }
  }

  "Emulator" should "run a simple 48k basic program which changes the border and sets some pixels" taggedAs UITest in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.typeProgram("B0")
    fixture.typeProgram("O16384,255")

    // then
    forEvery(display.getBorderColours) { _ shouldBe 0xff000000 }
    forEvery(1 to 6) { x =>
      display.getTargetPixels(targetPixelAt(x, 0, 0, 0)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 0, 1)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 1, 0)) shouldBe 0xff000000
      display.getTargetPixels(targetPixelAt(x, 0, 1, 1)) shouldBe 0xff000000
    }
  }

  it should "run a simple 128k basic program" taggedAs UITest in {
    // given
    fixture.enter128kBasic()

    // when
    fixture.typeProgram("POKE 16384, 255")

    // then
    emulator.peek(16384) shouldBe 255
  }

  (it should "handle Sinclair joystick input correctly").taggedAs(UITest) in {
    // given
    fixture.enter48kBasic()

    // when
    fixture.menuItem("JoystickSinclair").click()
    fixture.typeProgram("LB=Q+A+O+P+M")

    // and
    fixture.menuItem("JoystickNone").click()
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

class TestDisplayComponent extends DisplayComponent {
  def getBorderColours: Array[Int] = borderImageDataBuffer
  def getTargetPixels: Array[Int] = imageDataBuffer
}

class InspectableEmulator(frame: JFrame, prefs: UserPreferences, display: DisplayComponent)
  extends Emulator(frame, prefs, display) {
  def peek(addr: Int): Int = computer.peek(addr)
  def reset(): Unit = {
    computer = newComputer(Model.PLUS_2)
    resetComputer()
  }
}