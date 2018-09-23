package com.socialthingy.plusf.replist

import com.socialthingy.replist.RepList
import org.scalatest.{FlatSpec, Matchers}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class RepListSpec extends FlatSpec with Matchers {

  def rl = {
    val list = new RepList[String]()
    list.add("A", 3)
    list.add("B", 2)
    list.add("C", 4)
    list
  }

  "an empty RepList" should "never iterate" in {
    val emptyList = new RepList[String]()
    emptyList.iterator().hasNext shouldBe false
  }

  "a RepList" should "return all repetitions when iterated through" in {
    val result = collectAll(rl.iterator())
    result.mkString shouldBe "AAABBCCCC"
  }

  it should "skip a single repetition correctly within a single item" in {
    val iter = rl.iterator()
    val result = ListBuffer[String]()

    result += iter.next()
    val skipped = iter.skip(1)
    while (iter.hasNext) {
      result += iter.next()
    }

    skipped shouldBe 1
    result.mkString shouldBe "AABBCCCC"
  }

  it should "skip all repetitions correctly within a single item" in {
    val iter = rl.iterator()
    val result = ListBuffer[String]()

    val skipped = iter.skip(3)
    while (iter.hasNext) {
      result += iter.next()
    }

    skipped shouldBe 3
    result.mkString shouldBe "BBCCCC"
  }

  it should "skip two repetitions across two items" in {
    val iter = rl.iterator()
    val result = ListBuffer[String]()

    result += iter.next()
    result += iter.next()

    val skipped = iter.skip(2)

    while (iter.hasNext) {
      result += iter.next()
    }

    skipped shouldBe 2
    result.mkString shouldBe "AABCCCC"
  }

  it should "skip four repetitions across three items" in {
    val iter = rl.iterator()
    val result = ListBuffer[String]()

    result += iter.next()
    result += iter.next()

    val skipped = iter.skip(4)

    while (iter.hasNext) {
      result += iter.next()
    }

    skipped shouldBe 4
    result.mkString shouldBe "AACCC"
  }

  it should "stop skipping when the end of the list is reached" in {
    val iter = rl.iterator()
    val skipped = iter.skip(10)

    skipped shouldBe 9
    iter.hasNext shouldBe false
  }

  def collectAll[T](rl: java.util.Iterator[T]): List[T] = {
    @tailrec
    def loop(acc: List[T]): List[T] = {
      if (rl.hasNext) {
        loop(rl.next() :: acc)
      } else {
        acc.reverse
      }
    }

    loop(List[T]())
  }

}
