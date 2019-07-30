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
  * SchemaToStringSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-24 17:18
  */
class SchemaToStringSpec extends SparkSpec with StrictLogging {

  import spark.implicits._
  OuterScopes.addOuterScope(this)
  case class User(name: String, age: Int, fav: List[Int])

  val df = Seq(
    User("a", 10, List(1, 2, 3)),
    User("a", 10, List(1, 2, 3)),
    User("a", 10, List(1, 2, 3)),
    User("a", 10, List(1, 2, 3))
  ).toDF

  "schema to string" in {
    val schema = df.schema
    logger.info(schema.toString())
    logger.info(schema.prettyJson)
    logger.info(schema.simpleString)
    logger.info(schema.toDDL)
    df.show()
  }
}
