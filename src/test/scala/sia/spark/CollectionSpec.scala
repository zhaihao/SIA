/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

/**
  * CollectionSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-04 11:13
  */
class CollectionSpec extends SparkSpec {
  import spark.implicits._
  "集合最小" in {

    Seq(
      (1, "2019-07-01 10:10:10", "2019-07-01 10:12:00"),
      (1, "2019-07-01 10:10:10", "2019-07-01 10:13:00"),
      (1, "2019-07-01 10:10:10", "2019-07-02 10:14:00"),
      (1, "2019-07-01 10:10:10", "2019-07-02 10:15:00"),
      (1, "2019-07-01 10:10:10", "2019-07-03 10:16:00")
    ).toDF("id", "ct", "lt").createOrReplaceTempView("log")

    spark.sql("""select id,
                |first(to_date(ct)) as regt,
                |array_min(
                |  collect_set(
                |     if(to_date(ct)=to_date(lt), null, to_date(lt))
                |  )
                |) as r
                |from log group by id""".stripMargin).show

  }

  "高阶函数 filter 用法" in {
    Seq(
      (1, "2019-07-01 10:12:00"),
      (1, "2019-07-01 10:13:00"),
      (1, "2019-07-02 10:14:00"),
      (1, "2019-07-02 10:15:00"),
      (1, "2019-07-03 10:16:00")
    ).toDF("id", "lt").createOrReplaceTempView("log")

    Seq(
      (1, "2019-07-01 10:10:00")
    ).toDF("id", "rt").createOrReplaceTempView("reg")

    spark.sql("""
                |select
                | id,
                | to_date(rt) as rt
                |from reg
      """.stripMargin).createOrReplaceTempView("t_reg")

    val collect = spark.sql("""
                              |select
                              | id,
                              | collect_set(to_date(lt)) as lts
                              |from log
                              |group by id
      """.stripMargin)

    collect.show(false)

    collect.createOrReplaceTempView("t_log")

    val filter = spark.sql("""
                             |select
                             | a.id,
                             | a.rt,
                             | filter(b.lts, x -> x != a.rt) as lts
                             |from t_reg a, t_log b
                             |where a.id = b.id
      """.stripMargin)

    filter.show(false)

    filter.createOrReplaceTempView("t_filter")

    spark.sql("""
                |select
                | id,
                | rt,
                | array_min(lts) as min_lt,
                | array_max(lts) as max_lt,
                | datediff(array_min(lts),rt) as x,
                | datediff(array_max(lts),rt) as y
                |from t_filter
      """.stripMargin).show(false)
  }
}
