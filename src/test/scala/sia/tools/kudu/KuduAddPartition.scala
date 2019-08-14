/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools.kudu

import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec
import com.github.nscala_time.time.Imports._
import org.joda.time.Days

/**
  * KuduAddPartition
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-13 15:34
  */
class KuduAddPartition extends BaseSpec with StrictLogging {
  "test" in {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd")
    val begin  = DateTime.parse("2020-02-01", format)
    val end    = DateTime.parse("2020-03-01", format)
    val period = 1.days

    var s = begin
    while(s < end){
      println(s"""ALTER TABLE rt.server_log ADD RANGE PARTITION "${s.toString(format)}\\0"<= VALUES<"${s.plus(period).toString(format)}\\0";""")
      s = s.plus(period)
    }
  }

  "num" in {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd")
    val begin  = DateTime.parse("2019-06-28", format)
    val end    = DateTime.parse("2020-01-01", format)
    logger.info((begin to end).toDuration.getStandardDays.toString)
  }
}
