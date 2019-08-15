/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia
import bloomfilter.mutable.UnsafeBitArray
import org.openjdk.jmh.annotations.Benchmark

/**
  * UnsafeBitArrayBenchmark
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-15 13:35
  */
class UnsafeBitArrayBenchmark {

  import UnsafeBitArrayBenchmark._

  @Benchmark
  def setOp = {
    bs.set(1000000)
  }

  @Benchmark
  def getOp = {
    bs.get(1000000)
  }

  @Benchmark
  def fsetOp = {
    bs.fset(1000000)
  }

}

object UnsafeBitArrayBenchmark {
  val bs = new UnsafeBitArray(1024 * 1024 * 1024)
}