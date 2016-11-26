package com.socialthingy.plusf

trait Timing {
  def timed(timesToRun: Int)(code: => Unit): Long = {
    val startTime = System.currentTimeMillis
    (0 until timesToRun) foreach { _ => code }
    System.currentTimeMillis - startTime
  }
}
