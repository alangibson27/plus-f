package com.socialthingy.plusf.tape

import com.socialthingy.plusf.tape.TapeBlock.Bit
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.annotation.tailrec

trait TapeMatchers {
  val high = true
  val low = false

  def lowSignal: SignalState = new SignalState(low)
  def highSignal: SignalState = new SignalState(high)

  def haveLengthAndState(length: Int, state: Boolean): Matcher[List[Bit]] =
    new PulseSequenceMatcher(length, state)

  class PulseSequenceMatcher(length: Int, state: Boolean) extends Matcher[List[Bit]] {
    override def apply(left: List[Bit]): MatchResult = MatchResult(
      left.length == length && left.forall(_.getState == state),
      s"pulse was not $length bits of ${if (state) "high" else "low"}, it was ${left.length} bits",
      s"pulse was $length bits of ${if (state) "high" else "low"}"
    )
  }

  implicit class BitListOps(bl: List[Bit]) {
    def splitInto(counts: Int*): List[List[Bit]] = {
      @tailrec
      def loop(acc: List[List[Bit]], bits: List[Bit], counts: List[Int]): List[List[Bit]] =
        if (bits.isEmpty) {
          acc
        } else {
          counts match {
            case Nil => acc :+ bits
            case idx :: t => loop(acc :+ bits.take(idx), bits.drop(idx), t)
          }
        }

      loop(List[List[Bit]](), bl, counts.toList)
    }
  }
}
