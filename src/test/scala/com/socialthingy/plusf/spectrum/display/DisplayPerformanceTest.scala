package com.socialthingy.plusf.spectrum.display

import java.awt.Color

import scala.util.Random.nextInt

object DisplayPerformanceTest extends App {

  val displays: List[(Display, String)] = List(
//    (new BasicDisplay, "basic display"),
//    (new JavaFXDoubleSizeDisplay(), "JavaFXDoubleSizeDisplay"),
    (new JavaFXDoubleSizeDisplayWithoutLookups(), "JavaFXDoubleSizeDisplayWithoutLookups")
  )

  val unsafeDisplays: List[(UnsafeDisplay, String)] = List(
    (new UnsafeJavaFXDoubleSizeDisplay(), "UnsafeJavaFXDoubleSizeDisplay")
  )

  val repetitions = 1000
  val screens = (0 until repetitions) map { _ => Array.fill[Int](0x10000)(nextInt(0x100))}

  unsafeDisplays foreach {
    case (display, name) =>
      print(s"$name ... ")
      val screenIterator = screens.iterator
      val timeTaken = timed(repetitions) {
        val screen = screenIterator.next()
        (0 until 10) foreach { _ =>
          display.renderMemory(screen, false)
        }
      }

      println(timeTaken)
  }

  displays foreach {
    case (display, name) =>
      print(s"$name ... ")
      val screenIterator = screens.iterator
      val timeTaken = timed(repetitions) {
        val screen = screenIterator.next()
        (0 until 10) foreach { _ =>
          display.renderMemory(screen, false)
        }
      }

      println(timeTaken)
  }

  private def timed(timesToRun: Int)(code: => Unit): Long = {
    val startTime = System.currentTimeMillis
    (0 until timesToRun) foreach { _ => code }
    System.currentTimeMillis - startTime
  }

  class BasicDisplay extends Display(16, 16) {
    val pixels = Array.ofDim[Int](256, 192)
    override def renderMemory(memory: Array[Int], flashActive: Boolean): Unit = {
      super.draw(memory, flashActive, new DisplayPixelUpdate {
        override def update(x: Int, y: Int, colour: Color): Unit = pixels(x)(y) = colour.getRGB()
      })
    }
  }

  class JavaFXSingleSizeDisplay extends JavaFXDoubleSizeDisplay {
    override def scale(): Unit = ()
  }
}
