/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import test.BaseSpec

/**
  * SparkSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-06-12 11:19
  */
trait SparkSpec extends BaseSpec with BeforeAndAfterEach with BeforeAndAfterAll {

  val output = os.pwd / 'output / 'spark

  override def beforeEach() = {
    os.remove.all(output)
  }

  override def afterAll() = {
    spark.stop()
  }

  val spark =
    SparkSession
      .builder()
      .appName("spark test application")
      .master("local[*]")
      .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
      .getOrCreate()
}
