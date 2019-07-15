/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

/**
  * JoinSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-11 09:50
  */
class JoinSpec extends SparkSpec {
  import spark.implicits._
  /**
    * full join 返回 3 部分数据：
    *   1. 左表有的，右表没有的，右表置为 null
    *   2. 左表没有的，右表有的，左表置为 null
    *   3. 左表有的，右表有的，返回匹配数据的笛卡尔积
    */
  "full join" - {
    "two tables" in {

      Seq(
        (1, 30),
        (2, 40),
        (3, 50)
      ).toDF("id", "score").createOrReplaceTempView("t_score")

      Seq(
        (1, "李五"),
        (4, "张三")
      ).toDF("id", "name").createOrReplaceTempView("t_name")

      spark.sql("""
                  |select
                  |  a.*,
                  |  b.*
                  |from t_score a
                  |   full join t_name b
                  |     on a.id = b.id
        """.stripMargin).show()
    }

    "three tables" in {
      Seq(
        (1, 30),
        (2, 40),
        (3, 50),
        (5, 50)
      ).toDF("id", "score").createOrReplaceTempView("t_score_1")

      Seq(
        (1, "李五"),
        (4, "张三"),
        (5, "王五"),
        (7, "王七")
      ).toDF("id", "name").createOrReplaceTempView("t_name_1")

      Seq(
        (1,"男"),
        (4,"女"),
        (2,"男"),
        (6,"男")
      ).toDF("id","sex").createOrReplaceTempView("t_sex_1")

      // 多表的 full join 第二个以后的 join 应该用 or，更加符合期望
      spark.sql("""
                  |select
                  |  a.*,
                  |  b.*,
                  |  c.*
                  |from t_score_1 a
                  |   full join t_name_1 b
                  |     on a.id = b.id
                  |   full join t_sex_1 c
                  |     on a.id = c.id or b.id = c.id
                """.stripMargin).show()
    }
  }

  "inner join" - {
    "two tables" in {

      Seq(
        (1, 30),
        (2, 40),
        (3, 50)
      ).toDF("id", "score").createOrReplaceTempView("t_score")

      Seq(
        (1, "李五"),
        (4, "张三")
      ).toDF("id", "name").createOrReplaceTempView("t_name")

      spark.sql("""
                  |select
                  |  a.*,
                  |  b.*
                  |from t_score a
                  |   inner join t_name b
                  |     on a.id = b.id
                """.stripMargin).show()
    }

    "three tables" in {
      Seq(
        (1, 30),
        (2, 40),
        (3, 50),
        (5, 50)
      ).toDF("id", "score").createOrReplaceTempView("t_score_1")

      Seq(
        (1, "李五"),
        (4, "张三"),
        (5, "王五"),
        (7, "王七")
      ).toDF("id", "name").createOrReplaceTempView("t_name_1")

      Seq(
        (1,"男"),
        (4,"女"),
        (2,"男"),
        (6,"男")
      ).toDF("id","sex").createOrReplaceTempView("t_sex_1")

      spark.sql("""
                  |select
                  |  a.*,
                  |  b.*,
                  |  c.*
                  |from t_score_1 a
                  |   inner join t_name_1 b
                  |     on a.id = b.id
                  |   inner join t_sex_1 c
                  |     on a.id = c.id
                """.stripMargin).show()
    }
  }
}
