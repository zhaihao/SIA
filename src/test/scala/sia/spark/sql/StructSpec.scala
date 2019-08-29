/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark.sql

import sia.spark.SparkSpec
import test.BaseSpec

/**
  * StructSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019/8/28 5:30 下午
  */
class StructSpec extends SparkSpec {
  import spark.implicits._
  "构造一个 struct" in {
    Seq(
      (1, "a", 31),
      (2, "b", 32),
      (3, "c", 33),
      (4, "d", 34)
    ).toDF("id", "name", "age").createOrReplaceTempView("t_user")

    // language=SQL
    val res = spark.sql("""
                          |select
                          |  id,
                          |  struct(name,age,1 as l) as j
                          |from t_user
                          |""".stripMargin)

    res.show()
    res.printSchema()
  }

  "named struct" in {
    val df = spark.sql("select named_struct('a',1)")
    df.show()
    df.printSchema()
  }
}
