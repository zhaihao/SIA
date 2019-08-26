/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.oslib

import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019/8/26 2:22 下午
  */
class QuickStartSpec extends BaseSpec with StrictLogging {

  "常用路径" - {
    "jvm resources" in {
      val s = os.read(os.resource / "logback.xml")
      logger.info(s)
    }
  }

  "读取文件" - {
    "读取为字符串" in {
      val s: String = os.read(os.pwd / "version.sbt")
      logger.info(s)
    }
  }
}
