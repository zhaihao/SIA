/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

/**
  * ArraySpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-29 17:15
  */
class ArraySpec extends SparkSpec {

  import spark.implicits._

  "行的最大值" in {
    Seq(
      ("x", 1, 2, 3, 4),
      ("y", 3, 2, 3, 4)
    ).toDF("id", "a", "b", "c", "d").createTempView("t")

    spark.sql("select id, array_max(array(a,b,c,d)) from t").show()

  }
}
