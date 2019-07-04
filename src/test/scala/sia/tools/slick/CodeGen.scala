/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools.slick

import com.typesafe.scalalogging.StrictLogging
import slick.model.Model
import test.BaseSpec

import scala.collection.mutable

/**
  * CodeGen
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-04 13:43
  */
class CodeGen extends BaseSpec with StrictLogging {
  "生成代码" in {
    logger.info("begin generate slick code")

    SourceCodeGenerator.run(
      url =
        "jdbc:mysql://192.168.0.242:3300/XDataDB?characterEncoding=utf8&useSSL=false&nullNamePatternMatchesAll=true&tinyInt1isBit=false",
      profile               = "slick.jdbc.MySQLProfile",
      jdbcDriver            = "com.mysql.cj.jdbc.Driver",
      outputDir             = "/Users/zhaihao/code/XData/domain/src/main/scala",
      pkg                   = "xdata.domain3",
      user                  = Some("root"),
      password              = Some("mysql321o"),
      ignoreInvalidDefaults = true,
      codeGeneratorClass    = Some("sia.tools.slick.CustomizedCodeGenerator"),
      outputToMultipleFiles = false
    )

    logger.info("generate successfully")
  }
}

/**
  * case class 生成到 Tables 外部，兼容 play json format
  */
class CustomizedCodeGenerator(model: Model) extends SourceCodeGenerator(model) {
  val models = new mutable.MutableList[String]

  override def packageCode(profile:    String,
                           pkg:        String,
                           container:  String,
                           parentType: Option[String]): String = {
    super.packageCode(profile, pkg, container, parentType) + "\n" + outsideCode
  }

  def outsideCode = s"${indent(models.mkString("\n"))}"

  /**
    * Moves the Row(s) outside of the auto-generated 'trait Tables'
    */
  override def Table = new Table(_) {
    override def EntityType = new EntityTypeDef {
      override def docWithCode: String = {
        models += super.docWithCode.toString + "\n"
        ""
      }
    }
  }
}
