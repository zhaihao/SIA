/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.scalapb

import com.typesafe.scalalogging.StrictLogging
import sia.message.Person
import sia.message.Person.{PhoneNumber, PhoneType}
import test.BaseSpec

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-01 19:18
  */
class QuickStartSpec extends BaseSpec with StrictLogging {

  val p = Person("Tom",
                 1,
                 "qq@gmail.com",
                 Seq(PhoneNumber("110", PhoneType.WORK), PhoneNumber("119", PhoneType.HOME)))

  "toString" in {
    logger.info(p.toString)
    logger.info("\n" + p.toProtoString)
    logger.info(p.toPMessage.toString)
    logger.info(p.toByteString.toString)
    logger.info(p.toByteString.toStringUtf8)
  }

  "修改" in {
    val p1 = p.withId(10)
    logger.info(p.toProtoString)
    logger.info(p1.toProtoString)
  }

}
