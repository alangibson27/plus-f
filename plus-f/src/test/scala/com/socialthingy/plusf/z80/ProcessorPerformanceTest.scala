package com.socialthingy.plusf.z80

import java.io.InputStream

import com.socialthingy.plusf.Timing

import scala.annotation.tailrec

object ProcessorPerformanceTest extends App with Timing {

  val memory = new SimpleMemory
  val rom = getClass.getResourceAsStream("/48.rom")
  readMemory(rom, memory)

  val repetitions = 10000000
  val processor = new ProcessorHarness(new Processor(memory, new DummyIO))

  val harnesses = List(processor)
  harnesses foreach { harness =>
    print(s"${harness.toString} ...")
    val time = timed(repetitions) {
      harness.run()
    }
    println(time)
  }

  def readMemory(data: InputStream, destination: Memory): Unit = {
    @tailrec
    def loop(addr: Int): Unit = {
      val nextVal = data.read()
      if (nextVal >= 0) {
        memory.set(addr, nextVal)
        loop(addr + 1)
      }
    }

    loop(0)
  }
}

class DummyIO extends IO {
  override def recognises(low: Int, high: Int): Boolean = false
  override def write(port: Int, accumulator: Int, value: Int): Unit = ()
  override def read(port: Int, accumulator: Int): Int = 0
}

trait Harness {
  def run(): Unit
}

class ProcessorHarness(val processor: Processor) extends Harness {
  override def run(): Unit = processor.execute()
}