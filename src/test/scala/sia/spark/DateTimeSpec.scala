/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

/**
  * DateTimeSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-06-12 11:29
  */
class DateTimeSpec extends SparkSpec {
  "date_format" - {
    ",SSS 的毫秒无法格式化" in {
      spark.sql("select date_format('2019-05-01 19:22:31,123','yyyy-MM-dd hh:mm:ss,SSS')").show(false)
    }

    ".SSS 的毫秒可以格式化" in {
      spark.sql("select date_format('2019-05-01 19:22:31.123','yyyy-MM-dd hh:mm:ss,SSS')").show(false)
    }
  }
}
