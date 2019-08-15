/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.jmh_example

import org.openjdk.jmh.annotations.Benchmark

/**
  * JMH_01_HelloWorld
  *
  * jmh:run -i 10 -wi 10 -f 1 -t 1 sia.jmh_example.JMH_01*
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-15 13:42
  */
class JMH_01_HelloWorld {

  @Benchmark
  def doNothing = {}
}
