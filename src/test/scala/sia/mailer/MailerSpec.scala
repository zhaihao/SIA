/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.mailer

import com.typesafe.scalalogging.StrictLogging
import mailer._
import sia.tools.pwd
import test.BaseSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * MailerSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-18 16:39
  */
class MailerSpec extends BaseSpec with StrictLogging {

  val domain = "smtp.51nbapi.com"
  val user   = "dc_notify@51nbapi.com"

  val mailerSettings = MailerSettings(
    protocol = Some("smtp"),
    host     = domain,
    port     = "25",
    failTo   = "dc_notify@51nbapi.com",
    auth     = Some(true),
    username = Some(user),
    password = Some(pwd(domain, user))
  )

  "test" in {
    val email = Email(
      subject     = "Test mail",
      from        = EmailAddress("dc_notify", "dc_notify@51nbapi.com"),
      text        = "text", // 邮件能同时发送文本与 html 内容。邮件服务商默认显示 html 内容
      htmlText    = "htmlText",
      replyTo     = Some(EmailAddress("dc_notify", "dc_notify@51nbapi.com")),
      recipients  = List(Recipient(RecipientType.TO, EmailAddress("zhaihao", "zhaihao@ooon.me"))),
      attachments = Seq.empty
    )

    Await.result(new Mailer(Session.fromSetting(mailerSettings)).sendEmail(email), 20.seconds)
  }

}
