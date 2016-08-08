package com.socialthingy.replist

import scala.collection.mutable.ListBuffer

class RepList[T] extends java.lang.Iterable[T] {
  private val items = ListBuffer[Rep[T]]()
  def append(other: RepList[T]): Unit = items.append(other.items: _*)
  def add(item: T, reps: Int): Unit = items += new Rep(item, reps)
  override def iterator(): SkippableIterator[T] = new RepIterator

  class RepIterator extends SkippableIterator[T] {
    var (count, item) = {
      if (items.nonEmpty) {
        val first = items.remove(0)
        (first.reps, first.item)
      } else {
        (0, null.asInstanceOf[T])
      }
    }

    private def nextItem() = {
      if (count == 0 && items.nonEmpty) {
        val nextRep = items.remove(0)
        count = nextRep.reps
        item = nextRep.item
      }
    }

    override def next(): T = {
      val toReturn = item
      count = count - 1
      nextItem()
      toReturn
    }

    override def hasNext: Boolean = count > 0 || items.nonEmpty
    override def skip(amount: Int): Int = {
      require(amount >= 0)
      if (amount <= count) {
        count = count - amount
        nextItem()
        amount
      } else {
        val taken = count
        count = 0
        nextItem()
        if (hasNext) {
          taken + skip(amount - taken)
        } else {
          taken
        }
      }
    }
  }
}

trait SkippableIterator[T] extends java.util.Iterator[T] {
  def skip(count: Int): Int
}

private[replist] class Rep[T](val item: T, val reps: Int)