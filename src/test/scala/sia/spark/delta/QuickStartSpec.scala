/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark.delta

import com.typesafe.scalalogging.StrictLogging
import io.delta.tables.DeltaTable
import sia.spark.SparkSpec
import org.apache.spark.sql.functions._

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-19 15:30
  */
class QuickStartSpec extends SparkSpec with StrictLogging {

  val table = (output / "delta_table").toString()

  val delta = "delta"

  "create" in {
    val data = spark.range(0, 5)
    data.write.format(delta).save(table)
  }

  "read" in {
    val data = spark.range(0, 5)
    data.write.format(delta).save(table)
    spark.read.format(delta).load(table).show()
  }

  "update" - {
    "overwrite" in {
      val data = spark.range(0, 5)
      data.write.format(delta).save(table)
      spark.range(5, 10).write.mode("overwrite").format(delta).save(table)
      spark.read.format(delta).load(table).show()
    }

    "upsert" in {
      val data = spark.range(0, 5)
      data.write.format(delta).save(table)
      val deltaTable = DeltaTable.forPath(table)

      val newData = spark.range(0, 20).as("newData")

      deltaTable
        .as("oldData")
        .merge(newData.toDF, "oldData.id = newData.id")
        .whenMatched()
        .update(Map("id" -> col("newData.id")))
        .whenNotMatched()
        .insert(Map("id" -> col("newData.id")))
        .execute()

      deltaTable.toDF.show()

    }
  }

  "delete" in {
    val data = spark.range(0, 5)
    data.write.format(delta).save(table)
    val df = spark.read.format(delta).load(table)
    df.show()

    val deltaTable = DeltaTable.forPath(table)
    deltaTable.delete(condition = expr("id % 2 ==0"))
    df.show()
    spark.read.format(delta).load(table).show()
  }

  "version" in {
    spark.range(0, 5).write.format(delta).save(table)                    // v0
    spark.range(5, 10).write.mode("overwrite").format(delta).save(table) // v1

    val deltaTable = DeltaTable.forPath(table)
    val newData    = spark.range(0, 20).as("newData")
    deltaTable
      .as("oldData")
      .merge(newData.toDF, "oldData.id = newData.id")
      .whenMatched()
      .update(Map("id" -> col("newData.id")))
      .whenNotMatched()
      .insert(Map("id" -> col("newData.id")))
      .execute() //v2

    spark.read.format(delta).option("versionAsOf", 0).load(table).show()
    spark.read.format(delta).option("versionAsOf", 1).load(table).show()
    spark.read.format(delta).option("versionAsOf", 2).load(table).show()
  }
}
