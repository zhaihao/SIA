/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

import com.typesafe.scalalogging.{Logger, StrictLogging}
import org.apache.spark.TaskContext
import org.apache.spark.sql.Row
import test.BaseSpec

/**
  * PartitionByContextSpec
  *
  * Dataset 按某一列的值进行分布
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-07 18:37
  */
class PartitionByContextSpec extends BaseSpec with SparkSpec with StrictLogging {

  import spark.implicits._
  import org.apache.spark.sql.functions._
  val output = os.pwd / 'output / 'spark

  "Dataset API" in {
    val df = Seq(
      ("Warsaw1", 2016, 100),
      ("Warsaw2", 2017, 200),
      ("Warsaw3", 2016, 100),
      ("Warsaw4", 2017, 200),
      ("Beijing1", 2017, 200),
      ("Beijing2", 2017, 200),
      ("Warsaw4", 2017, 200),
      ("Boston1", 2015, 50),
      ("Boston2", 2016, 150)
    ).toDF("city", "year", "amount")

    val dropRight = udf { (s: String, a: Int) =>
      s.dropRight(a)
    }

    os.remove.all(output)
    df.repartition(3, dropRight($"city", lit(1))).write.format("csv").save(output.toString())
  }
}
