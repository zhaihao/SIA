/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.requests

import com.typesafe.scalalogging.StrictLogging
import requests.Response
import test.BaseSpec

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-20 16:35
  */
class QuickStartSpec extends BaseSpec with StrictLogging {
  val host = "http://httpbin.org"

  def loggerResponse(r:Response) = {
    logger.info(r.statusCode.toString)
    logger.info(r.statusMessage)
    logger.info("\n" + r.text())
  }

  "get" - {
    "sample" in {
      val r = requests.get(host + "/get")
      loggerResponse(r)
    }
  }

  "post" - {
    "data" in {
      val r = requests.post(host + "/post", data = Map("k" -> "v"))
      loggerResponse(r)
    }
  }
}
