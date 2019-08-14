/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools.azkaban

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json
import sia.tools.pwd
import test.BaseSpec
import com.github.nscala_time.time.Imports._

/**
  * AzkabanUtil
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-13 10:55
  */
class AzkabanUtil extends BaseSpec with StrictLogging {

  val server   = "http://172.18.20.231:8081"
  val user     = "azkaban"
  val password = pwd(server.substring(7), user)
  val project  = "xygj_user_register_report"
  val flow     = "h5_regist_rt"

  val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val start  = DateTime.parse("2019-08-13 09:01:00", format)
  val end    = DateTime.parse("2019-08-13 11:01:00", format)
  val period = 15.minutes.toPeriod

  val headers = Seq(
    "Content-Type"     -> "application/x-www-form-urlencoded",
    "X-Requested-With" -> "XMLHttpRequest"
  )

  // 修改时间，修改test 名字，并发执行
  "test 08-13" in {
    logger.warn(s"${start.toString(format)} - ${end.toString(format)}")
    val sessionId = login
    var s        = start
    var continue = true
    while (continue && s < end) {
      logger.info("")
      logger.warn(s"开始处理 ${s.toString(format)}")
      var execId = exec(sessionId, s.toString(format))
      var n      = 1
      while (execId == -1 && n <= 12) {
        logger.error(s"重试第 $n 次(${s.toString(format)})")
        execId = exec(sessionId, s.toString(format))
        n += 1
        Thread.sleep(5000)
      }
      if (execId == -1) {
        sys.error(s"无法提交任务(${s.toString(format)})")
      }
      var fetchLoop = true
      while (fetchLoop) {
        val status = fetchResult(sessionId, execId)
        logger.warn(s"${s.toString(format)} -> $execId -> $status")
        if (status == "SUCCEEDED") {
          fetchLoop = false
        } else if (status == "FAILED") {
          fetchLoop = false
          continue  = false
          logger.error(s"${s.toString(format)} exec error")
        } else {
          Thread.sleep(5000)
        }

      }
      s = s.plus(period)
    }

  }

  def login = {
    val r = requests.post(server,
                          headers = headers,
                          data = Map(
                            "action"   -> "login",
                            "username" -> user,
                            "password" -> password
                          ))

    logger.warn("登录结果: \n" + r.text())

    (Json.parse(r.text()) \ "session.id").as[String]
  }

  def exec(sessionId: String, time: String) = {
    val r = requests.get(
      server + "/executor",
      headers = headers,
      data = Map(
        "ajax"                                       -> "executeFlow",
        "session.id"                                 -> sessionId,
        "project"                                    -> project,
        "flow"                                       -> flow,
        "concurrentOption"                           -> "ignore",
        "flowOverride[azkaban.flow.start.timestamp]" -> time
      )
    )

    logger.warn("执行：\n" + r.text())
    (Json.parse(r.text()) \ "execid").as[Int]
  }

  def fetchResult(sessionId: String, execId: Int) = {
    val r = requests.get(
      server + "/executor",
      headers = headers,
      data = Map(
        "ajax"       -> "fetchexecflow",
        "session.id" -> sessionId,
        "execid"     -> execId.toString
      )
    )

    (Json.parse(r.text()) \ "status").as[String]
  }
}
