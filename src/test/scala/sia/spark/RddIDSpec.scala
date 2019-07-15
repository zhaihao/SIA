/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

import com.typesafe.scalalogging.StrictLogging

class RddIDSpec extends SparkSpec with StrictLogging {

  import spark.implicits._

  "rdd id 是否会变化" in {
    val df = Seq(
      (1, 30),
      (2, 40),
      (3, 50)
    ).toDF("id", "score")

    logger.info("a:" + df.rdd.id)

    df.createOrReplaceTempView("t1")
    logger.info("b:" + df.rdd.id)
    df.createOrReplaceTempView("t1")
    logger.info("c:" + df.rdd.id)
    df.createOrReplaceTempView("t2")
    logger.info("d:" + df.rdd.id)

    val df2 = spark.sql("select * from t2")
    logger.info("e:" + df2.rdd.id)
  }
}
