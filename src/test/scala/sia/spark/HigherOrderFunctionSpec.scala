/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

import org.apache.spark.sql.Encoders
import org.apache.spark.sql.types.StructType

/**
  * HigherOrderFunctionSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-29 20:13
  */
class HigherOrderFunctionSpec extends SparkSpec {
  "test" in {
    val rdd = spark.sparkContext.parallelize(
      Seq(
        User2(1, Array(1, 2, 3), Array(2, 3, 4)),
        User2(2, Array(1, 2, 3), Array(3, 4, 5))
      ))


    spark.createDataFrame(rdd).createOrReplaceTempView("t")

    spark.sql("select * from t").show()
    spark.sql("select id ")
  }
}

case class User2(id: Int, a: Array[Int], b: Array[Int])
