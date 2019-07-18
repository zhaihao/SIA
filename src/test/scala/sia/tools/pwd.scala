/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools
import _root_.slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * pwd
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-18 17:05
  */
object pwd {

  private val db = Database.forURL(
    "jdbc:sqlite:/Users/zhaihao/Library/Mobile Documents/com~apple~CloudDocs/pwd.sqlite")

  def apply(domain: String, user: String) = {
    val p = Await.result(
      db.run(
        sql"select password from t_pwd where domain=$domain and user=$user".as[String].headOption),
      5.seconds)

    p.getOrElse(sys.error(s"$user @ $domain password is not exits."))
  }
}
