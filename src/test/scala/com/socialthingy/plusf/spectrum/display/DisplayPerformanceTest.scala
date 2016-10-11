package com.socialthingy.plusf.spectrum.display

import com.socialthingy.plusf.Timing

import scala.util.Random.nextInt

object DisplayPerformanceTest extends App with Timing {
/*
  val displays: List[(Renderer, String)] = List(
    (new JavaFXDoubleSizeDisplay(), "JavaFXDoubleSizeDisplay")
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
  */
}
