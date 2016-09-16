package com.socialthingy.plusf.spectrum.display

import scala.util.Random.nextInt

object DisplayPerformanceTest extends App {

  val displays: List[(Renderer, String)] = List(
    (new JavaFXDoubleSizeDisplay(), "JavaFXDoubleSizeDisplay"),
    (new JavaFXDoubleSizeDisplay2(), "JavaFXDoubleSizeDisplay2")
  )

  val repetitions = 1000
  val screens = (0 until repetitions) map { _ => Array.fill[Int](0x10000)(nextInt(0x100))}

  displays foreach {
    case (display, name) =>
      val screenIterator = screens.iterator
      val timeTaken = timed(repetitions) {
        val screen = screenIterator.next()
        (0 until 10) foreach { _ =>
          display.renderMemory(screen, false)
        }
      }
  }

  displays.reverse foreach {
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
}
