/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.tools

import java.sql.Date
import java.text.SimpleDateFormat

import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec
import _root_.slick.jdbc.GetResult
import _root_.slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * BlogApi
  *
  * 主要为了方便 mweb 笔记转 hexo 博客
  *
  * @author zhaihao
  * @version 1.0 2019-01-14 17:43
  */
//noinspection SqlResolve
class Blog extends BaseSpec with StrictLogging {

  case class ArticleInfo(uuid: Long, dateAdd: Long, dateModif: Long, tagName: String)

  val wd      = os.home / 'Library / "Mobile Documents" / "iCloud~com~coderforart~iOS~MWeb" / 'Documents / 'Notes
  val url     = s"jdbc:sqlite:$wd/mainlib.db"
  val output  = os.home / 'code / 'blog / 'source
  val db      = Database.forURL(url)
  val timeout = 10.seconds
  val sdf     = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  implicit val grArticle = GetResult(r => ArticleInfo(r.<<, r.<<, r.<<, r.<<))

  "同步" in {
    val posts = getModifyPosts
    if (posts.isEmpty) logger.warn("没有需要同步的文章")
    else {
      logger.info(s"即将同步 ${posts.length} 篇文章")
      posts.foreach(uuid => covert(uuid))
    }

    updateBlogTime
    logger.info("同步完成！")
  }

  "清理 sqlite 无效的 id" in {
    val ids: Seq[Long] = Await.result(
      db.run(
        sql"""select ta.id
              from tag_article ta
              left join article a on ta.aid=a.uuid
              where a.uuid is null"""
          .as[Long]
      ),
      timeout
    )

    logger.info(s"共存在 ${ids.length}条 无效的记录")

    ids.foreach { id =>
      Await.result(
        db.run(sqlu"""delete from tag_article where id=$id"""),
        timeout
      )
    }
  }

  def covert(uuid: Long) = {
    val file  = wd / 'docs / s"$uuid.md"
    val lines = os.read.lines(file)
    val title = lines.head.drop(2).trim
    val body  = lines.drop(1)

    logger.info(s"开始转换: [$title]($uuid)")

    val result     = find(uuid)
    val createTime = sdf.format(new Date(result.head.dateAdd * 1000))
    val updateTime = sdf.format(new Date(result.head.dateModif * 1000))
    val tags       = result.map(_.tagName)

    if (!tags.contains("Blog")) sys.error("这篇不是 blog 文章")

    val mediaFlag = body.exists(_.contains(s"media/$uuid"))

    if (mediaFlag) {
      logger.info("拷贝图片资源")
      os.remove.all(output / "media" / s"$uuid")
      os.copy(wd / 'docs / 'media / s"$uuid",
              output / "media" / s"$uuid",
              replaceExisting = true,
              createFolders   = true)
    }

    val header = s"""|title: $title
                     |date: $createTime
                     |updated: $updateTime
                     |tags:
                     |${tags.filter(_ != "Blog").mkString("- ", "\n- ", "")}
                     |---""".stripMargin

    logger.info("生成 header：" + "\n" + header)

    logger.info("写入 blog 内容")

    val blogFile = output / "_posts" / createTime.substring(0, 4) / createTime.substring(5, 7) / s"$title.md"
    if (os.exists(blogFile)) os.remove(blogFile)

    //language=RegExp
    val reg = """!\[.*-w(\d+)]\(media/\d+/(\d+.\S{3})\)""".r

    os.write.append(blogFile, header, createFolders = true)

    var i             = 1
    var canInsertMore = true
    var more          = false
    body.foreach { line =>
      if (!more && canInsertMore && i > 10) {
        os.write.append(blogFile, "<!--more-->" + "\n")
        more = true
      }

      line match {
        case s if s.trim == "$$" || s.trim.startsWith("```") =>
          os.write.append(blogFile, s + "\n")
          canInsertMore = !canInsertMore
        case reg(width, pic) =>
          val w = if (width.toInt > 600) 600 else width.toInt
          os.write.append(blogFile, s"""<img src="/media/$uuid/$pic" width=${w}px>""" + "\n")

        case "[toc]" => i -= 1
        case str =>
          os.write.append(blogFile, str + "\n")
      }
      i += 1
    }

    logger.info("博客生成完毕")
  }

  def find(uuid: Long) = Await.result(
    db.run(
      sql"""select
                a.uuid, a.dateAdd, a.dateModif, t.name
              from article a
              left join tag_article ta on a.uuid = ta.aid
              left join tag t          on ta.rid = t.id
              where a.uuid = $uuid"""
        .as[ArticleInfo]
    ),
    timeout
  )

  def getModifyPosts: Seq[Long] = {
    Await.result(
      db.run(
        sql"""select a.uuid
              from tag t
              left join tag_article ta on t.id = ta.rid
              left join article a      on ta.aid = a.uuid
              left join blog b         on a.dateModif > b.updateTime
              where t.name = 'Blog'
                and b.id = 1
                and a.uuid is not null"""
          .as[Long]
      ),
      timeout
    )
  }

  def updateBlogTime = {
    val now = System.currentTimeMillis()
    val dt  = now / 1000
    Await.result(
      db.run(sqlu"""update blog set  updateTime = $dt where id = 1"""),
      timeout
    )
    logger.info(s"记录本次同步的时间: ${sdf.format(new Date(now))}")
  }
}
