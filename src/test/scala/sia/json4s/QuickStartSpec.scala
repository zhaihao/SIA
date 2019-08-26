/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.json4s

import com.typesafe.scalalogging.StrictLogging
import org.json4s.JValue
import test.BaseSpec

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019/8/26 2:31 下午
  */
class QuickStartSpec extends BaseSpec with StrictLogging {
  "解析 json" in {
    import org.json4s.jackson.JsonMethods._
    val json: JValue = parse("""{"a":1,"b":"2"}""")
    logger.info(json.toString)
  }

  "修改字段值" in {
    import org.json4s.jackson.JsonMethods._
    val json: JValue = parse("""{"a":1,"b":"2"}""")

    import org.json4s.JsonDSL._

    val newJson = json.replace(List("b"),"22")
    logger.info(newJson.toString)
  }

  "添加字段" in {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    var json: JValue = parse("""{"a":1,"b":"2"}""")

    json = json merge JObject("c" -> JString("22")) merge JObject("b" -> JInt(2))
    logger.info(json.toString)
  }
}
