/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark.schema

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.catalyst.encoders.OuterScopes
import sia.spark.SparkSpec

/**
  * RenameStructSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-30 17:18
  */
class RenameStructSpec extends SparkSpec with StrictLogging {

  import spark.implicits._

  OuterScopes.addOuterScope(this)

  case class User(name:  String, age:   Int, scores: Array[Score])
  case class Score(name: String, score: Double)

  val df = Seq(
    User("Tom", 10, Array(Score("yw", 30.0), Score("sx", 40.0))),
    User("Lucy", 9, Array(Score("yw", 30.0), Score("sx", 40.0)))
  ).toDF

  "sql方式" in {
    df.printSchema()
    df.show(false)
    df.createOrReplaceTempView("t_user")
    //language=SQL
    val res = spark.sql("""
        select
            name,
            age,
            cast(scores as array<struct<name:string,s:int>>) as scores
        from t_user
      """.stripMargin)

    res.printSchema()
    res.show(false)
  }

  "withColumn" in {
    df.printSchema()
    df.show(false)
    val res = df.withColumn("scores", $"scores".cast("array<struct<name:string,s:int>>"))
    res.printSchema()
    res.show(false)
  }

  "withColumn with expr" in {
    import org.apache.spark.sql.functions._
    df.printSchema()
    df.show(false)
    val res = df.withColumn("scores", expr("cast(scores as array<struct<name:string,s:int>>)"))
    res.printSchema()
    res.show(false)
  }
}
