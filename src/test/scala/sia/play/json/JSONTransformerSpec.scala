/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.play.json

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._
import test.BaseSpec

/**
  * JSONTransformerSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019/9/3 1:49 下午
  */
class JSONTransformerSpec extends BaseSpec with StrictLogging {

  //language=JSON
  val json = Json.parse("""
                          |{
                          |  "key1" : "value1",
                          |  "key2" : {
                          |    "key21" : 123,
                          |    "key22" : true,
                          |    "key23" : [ "alpha", "beta", "gamma"],
                          |    "key24" : {
                          |      "key241" : 234.123,
                          |      "key242" : "value242"
                          |    }
                          |  },
                          |  "key3" : 234
                          |}
                          |""".stripMargin)

  "访问一个属性" in {
    val pick: Reads[JsValue]    = (__ \ 'key2 \ 'key23).json.pick
    val res:  JsResult[JsValue] = json.transform(pick)
    logger.info(res.toString)

    // 断言类型
    val pick2 = (__ \ 'key2 \ 'key23).json.pick[JsArray]
    val res2: JsResult[JsArray] = json.transform(pick2)
    logger.info(res2.toString)
  }

  "访问一个分支" in {
    val pickBranch = (__ \ 'key2 \ 'key24 \ 'key241).json.pickBranch
    val res        = json.transform(pickBranch)
    logger.info(res.toString)
  }

  "复制" in {
    val copy = (__ \ 'key25 \ 'key251).json.copyFrom((__ \ 'key2 \ 'key21).json.pick)
    val res  = json.transform(copy)
    logger.info(res.toString)
  }

  "内层结构加一个字段" in {
    val addField = (__ \ 'key2 \ 'key24).json.update(__.read[JsObject].map { o =>
      o ++ Json.obj("key243" -> "aaa")
    })

    val res = json.transform(addField).get
    logger.info(Json.prettyPrint(res))

    val a = json.as[JsObject] deepMerge Json.obj(
      "key2" -> Json.obj("key24" -> Json.obj("key243" -> "aaa")))
    logger.info(Json.prettyPrint(a))

    val b = json.as[JsObject] ++ Json.obj(
      "key2" -> Json.obj("key24" -> Json.obj("key243" -> "aaa")))
    logger.info(Json.prettyPrint(b))
  }

  "内层结构改一个字段" in {
    val addField = (__ \ 'key2 \ 'key24).json.update(__.read[JsObject].map { o =>
      o ++ Json.obj("key242" -> "aaa")
    })

    val res = json.transform(addField).get
    logger.info(Json.prettyPrint(res))

    val a = json.as[JsObject] deepMerge Json.obj(
      "key2" -> Json.obj("key24" -> Json.obj("key242" -> "aaa")))
    logger.info(Json.prettyPrint(a))

    val b = json.as[JsObject] ++ Json.obj(
      "key2" -> Json.obj("key24" -> Json.obj("key242" -> "aaa")))
    logger.info(Json.prettyPrint(b))
  }

  "删掉一个分支" in {
    val jsonTransformer = (__ \ 'key2 \ 'key22).json.prune
    val r               = json.transform(jsonTransformer).get
    logger.info(Json.prettyPrint(r))

  }

}
