package com.socialthingy.plusf.tape

import org.scalatest.matchers.{MatchResult, Matcher}
import java.lang.{ Boolean => JBoolean }

import scala.annotation.tailrec

trait TapeMatchers {
  val high = true
  val low = false

  def lowSignal: SignalState = new SignalState(low)
  def highSignal: SignalState = new SignalState(high)

  def haveLengthAndState(length: Int, state: JBoolean): Matcher[List[JBoolean]] =
    new PulseSequenceMatcher(length, state)

  class PulseSequenceMatcher(length: Int, state: JBoolean) extends Matcher[List[JBoolean]] {
    override def apply(left: List[JBoolean]): MatchResult = MatchResult(
      left.length == length && left.forall(_ == state),
      s"pulse was not $length bits of ${if (state) "high" else "low"}, it was ${left.length} bits",
      s"pulse was $length bits of ${if (state) "high" else "low"}"
    )
  }

  implicit class BitListOps(bl: List[JBoolean]) {
    def splitInto(counts: Int*): List[List[JBoolean]] = {
      @tailrec
      def loop(acc: List[List[JBoolean]], bits: List[JBoolean], counts: List[Int]): List[List[JBoolean]] =
        if (bits.isEmpty) {
          acc
        } else {
          counts match {
            case Nil => acc :+ bits
            case idx :: t => loop(acc :+ bits.take(idx), bits.drop(idx), t)
          }
        }

      loop(List[List[JBoolean]](), bl, counts.toList)
    }
  }
}
