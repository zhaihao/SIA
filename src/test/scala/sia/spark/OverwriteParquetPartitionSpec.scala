/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.SaveMode

/**
  * OverwriteParquetPartitionSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-25 16:45
  */
class OverwriteParquetPartitionSpec extends SparkSpec with StrictLogging{
  "test" in {
    import spark.implicits._
    val table  = "t_user"
    val df1 = Seq(
      (1, 2016),
      (2, 2016),
      (3, 2017)
    ).toDF("id", "y")

    df1.repartition($"y").write.partitionBy("y").parquet(output + "/" + table)

    val df2 = Seq(
      (1, 2019),
      (2, 2019),
      (3, 2018)
    ).toDF("id", "y")

    spark.read.option("basePath", output + "/" + table).parquet(output + "/" + table + "/y=2016").createOrReplaceTempView(table)

    spark.sql(s"select * from $table").show()

    df2.repartition($"y").write.option("basePath", output + "/" + table).format("parquet").mode(SaveMode.Overwrite).insertInto(table)
  }
}
