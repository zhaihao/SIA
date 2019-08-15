/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

/**
  * GenIdSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-15 15:51
  */
class GenIdSpec extends SparkSpec {

  import spark.implicits._

  /**
    * 不要使用 row_number 去生成 唯一id，monotonically_increasing_id
    * 函数保证 唯一性，但不保证连续性，另外要求 partition 数小于 10e，
    * 单partition 记录数小于 80e
    */
  "test" in {
    Seq(
      1,
      2,
      3
    ).toDF("no").createOrReplaceTempView("tt")

    spark.sql("select no, monotonically_increasing_id() as id from tt").show()
  }

}
