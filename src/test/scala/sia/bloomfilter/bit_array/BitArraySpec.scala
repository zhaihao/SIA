/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.bloomfilter.bit_array

import java.io.FileOutputStream

import bloomfilter.mutable.UnsafeBitArray
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * BitArraySpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-14 14:02
  */
class BitArraySpec extends BaseSpec with StrictLogging{
  "save" in {
    val bs = new UnsafeBitArray(1024 * 1024 * 1024)
    bs.set(5)
    val fos = new FileOutputStream("output/1.txt")
    bs.writeTo(fos)
    fos.flush()
    fos.close()
    bs.dispose()
  }

  "set fset" in {
    val bs = new UnsafeBitArray(1024 * 1024 * 1024)
    logger.info(bs.get(5).toString)
    bs.set(5)
    logger.info(bs.get(5).toString)
    bs.fset(5)
    logger.info(bs.get(5).toString)
    bs.dispose()
  }

  "test2" in {
    val bs = new UnsafeBitArray(1024 * 1024 * 30)
    (0 to 1000000).foreach{ _ =>
      1.toBinaryString
      bs.set(Random.nextInt(31457280))
    }
    var i = 0
    val res = ArrayBuffer.empty[Int]
    while (i < 31457280) {
      if(bs.get(i)){
        res.append(i)
      }
      i+=1
    }
    bs.dispose()
  }
}
