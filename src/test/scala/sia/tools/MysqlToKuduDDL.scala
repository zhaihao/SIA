/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools

import java.sql.{DriverManager, ResultSet}

import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

import scala.collection.mutable.ArrayBuffer

/**
  * MysqlToKuduDDL
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-06-20 17:51
  */
class MysqlToKuduDDL extends BaseSpec with StrictLogging {

  val url        = "jdbc:mysql://172.18.20.82:3306/base-user?characterEncoding=utf-8&useSSL=false"
  val user       = "datax"
  val password   = "XN4dataX@2017"
  val db         = "base-user"
  val table      = "user_info_ext"
  val primaryKey = Array("id", "gmt_create")
  val start      = "2019-06-28"
  val end        = "2021-01-01"
  val unit       = "day"
  val period     = 1

  "main" in {
    val fields = getFields(url, user, password, db, table)

    logger.info(s"总共 ${fields.length} 字段")

    val fs = fields
      .map { f =>
        s"""${f.COLUMN_NAME} ${convertDataType(f.DATA_TYPE)} ${convertNullAble(f.IS_NULLABLE)} ${processComment(
          f.COLUMN_COMMENT.trim)}"""
      }
      .mkString(",\n  ")

    val partition = buildPartition("values",
                                   LocalDate.parse(start),
                                   LocalDate.parse(end),
                                   unit,
                                   period).mkString("PARTITION ", ",\n    PARTITION ", "")

    val ddl =
      s"""CREATE TABLE ${db.replaceAll("-", "_")}_$table(
         |  $fs,
         |  PRIMARY KEY(id, gmt_create)
         |) PARTITION BY RANGE(gmt_create)
         |  (
         |    $partition
         |  )
         |STORED AS KUDU;
     """.stripMargin

    logger.info("\n" + ddl)
  }

  "table schema" in {
    val fields = getFields(url, user, password, db, table)
    logger.info(s"总共 ${fields.length} 字段")
    logger.info("\n" + fields.mkString("\n"))
  }

  "test buildPartition function" in {
    val res = buildPartition("values",
                             LocalDate.parse("2010-01-01"),
                             LocalDate.parse("2020-01-01"),
                             "year",
                             1).mkString("\n")

    logger.info("\n" + res)
  }

  case class Field(COLUMN_NAME:    String,
                   COLUMN_DEFAULT: String,
                   IS_NULLABLE:    String,
                   DATA_TYPE:      String,
                   COLUMN_COMMENT: String)

  def getFields(url: String, user: String, password: String, db: String, table: String) = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection = DriverManager.getConnection(url, user, password)
    val statement  = connection.createStatement()

    // language=SQL
    val res: ResultSet = statement.executeQuery(s"""
                                          select
                                          COLUMN_NAME,
                                          COLUMN_DEFAULT,
                                          IS_NULLABLE,
                                          DATA_TYPE,
                                          COLUMN_COMMENT
                                          from information_schema.COLUMNS
                                          where TABLE_SCHEMA='$db'
                                            and TABLE_NAME = '$table'
                                          order by ORDINAL_POSITION""")

    val fields    = ArrayBuffer.empty[Field]
    val keyFields = ArrayBuffer.empty[Field]
    while (res.next()) {
      val name = res.getString(1)
      if (primaryKey.contains(name)) {
        keyFields.append(Field(name, res.getString(2), "NO", res.getString(4), res.getString(5)))
      } else {
        fields.append(
          Field(name, res.getString(2), res.getString(3), res.getString(4), res.getString(5)))
      }

    }

    (keyFields ++ fields).toArray

  }

  def buildPartition(field: String, start: LocalDate, end: LocalDate, unit: String, period: Int) = {
    val d = unit match {
      case "day"   => period.days
      case "month" => period.months
      case "year"  => period.years
    }

    require(start < end, "start must less end")
    var s = start
    val buffer = ArrayBuffer.empty[(String, String)]
    buffer.append((null, s.toString))
    while (s < end) {
      val e = s + d
      if (e < end) {
        buffer.append((s.toString, e.toString))
      } else {
        buffer.append((s.toString, end.toString))
        buffer.append((end.toString, null))
      }

      s = e
    }

    buffer
      .map { t =>
        if (t._1 == null) {
          s"$field <= '${t._2}'"
        } else if (t._2 == null) {
          s"${t._1}' < $field"
        } else {
          s"'${t._1}' < $field <= '${t._2}'"
        }
      }
      .dropRight(1)
      .toArray
  }

  def convertDataType(DATA_TYPE: String) = DATA_TYPE.toLowerCase match {
    case "varchar"   => "string"
    case "bigint"    => "bigint"
    case "longtext"  => "string"
    case "datetime"  => "string"
    case "int"       => "int"
    case "tinyint"   => "TINYINT"
    case "decimal"   => "double"
    case "double"    => "double"
    case "timestamp" => "string"
    case "text"      => "string"
    case x           => throw new Exception(s"unsupported DATA_TYPE: $x")
  }

  def convertNullAble(IS_NULLABLE: String) = IS_NULLABLE.toLowerCase match {
    case "no"  => "not null"
    case "yes" => "null"
    case x     => throw new Exception(s"unsupported IS_NULLABLE: $x")
  }

  def processComment(comment: String) = {
    if (comment != null && comment.length > 0) s"comment '$comment'"
    else ""
  }

}
