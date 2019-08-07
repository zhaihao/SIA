/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

import sbt._

/**
  * Dependencies
  *
  * @author zhaihao
  * @version 1.0 2019-02-18 13:29
  */
object Dependencies extends AutoPlugin {
  override def requires = empty
  override def trigger  = allRequirements

  val spark_version = "2.4.1"

  object autoImport {
    // scala
    lazy val orison    = "me.ooon"                %% "orison"        % "0.0.21"
    lazy val scalatest = "org.scalatest"          %% "scalatest"     % "3.0.7" % Test
    lazy val os_lib    = "com.lihaoyi"            %% "os-lib"        % "0.2.8"
    lazy val requests  = "com.lihaoyi"            %% "requests"      % "0.1.7"
    lazy val play_json = "com.typesafe.play"      %% "play-json"     % "2.7.2"
    lazy val scalaz    = "org.scalaz"             %% "scalaz-core"   % "7.2.27"
    lazy val vegas     = "org.vegas-viz"          %% "vegas"         % "0.3.12-om"
    lazy val squants   = "org.typelevel"          %% "squants"       % "1.4.0"
    lazy val scraper   = "net.ruippeixotog"       %% "scala-scraper" % "2.1.0"
    lazy val nscala    = "com.github.nscala-time" %% "nscala-time"   % "2.22.0"

    // java
    lazy val sqlite = "org.xerial" % "sqlite-jdbc"          % "3.25.2"
    lazy val mysql  = "mysql"      % "mysql-connector-java" % "8.0.16"

    lazy val java_mail = Seq(
      "javax.mail"   % "javax.mail-api" % "1.6.2",
      "com.sun.mail" % "javax.mail"     % "1.6.2"
    )

    lazy val log = Seq(
      "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2",
      "ch.qos.logback"             % "logback-classic" % "1.2.3"
    )

    lazy val akka_version = "2.5.23"
    lazy val akka = Seq(
      "com.typesafe.akka" %% "akka-actor"              % akka_version,
      "com.typesafe.akka" %% "akka-slf4j"              % akka_version,
      "com.typesafe.akka" %% "akka-remote"             % akka_version,
      "com.typesafe.akka" %% "akka-http"               % "10.1.9",
      "com.typesafe.akka" %% "akka-cluster"            % akka_version,
      "com.typesafe.akka" %% "akka-persistence"        % akka_version,
      "com.typesafe.akka" %% "akka-cluster-metrics"    % akka_version,
      "com.typesafe.akka" %% "akka-testkit"            % akka_version % Test,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akka_version % Test
    )

    lazy val chill = Seq(
      "com.twitter" %% "chill"           % "0.9.3",
      "com.twitter" %% "chill-bijection" % "0.9.3"
    )

    // https://github.com/ghik/silencer
    lazy val silencer = Seq(
      compilerPlugin("com.github.ghik" %% "silencer-plugin" % "0.6"),
      "com.github.ghik" %% "silencer-lib" % "0.6"
    )

    val slick_version = "3.3.2"
    lazy val slick = Seq(
      "com.typesafe.slick" %% "slick"          % slick_version,
      "com.typesafe.slick" %% "slick-hikaricp" % slick_version,
      "com.typesafe.slick" %% "slick-codegen"  % slick_version % Test,
      "com.typesafe.slick" %% "slick-testkit"  % slick_version % Test
    )

    lazy val breeze = Seq(
      "org.scalanlp" %% "breeze"         % "1.0-RC2",
      "org.scalanlp" %% "breeze-natives" % "1.0-RC2",
      "org.scalanlp" %% "breeze-viz"     % "1.0-RC2"
    )

    lazy val spark = Seq(
      "org.apache.spark" %% "spark-core"                 % spark_version,
      "org.apache.spark" %% "spark-sql"                  % spark_version,
      "org.apache.spark" %% "spark-streaming"            % spark_version,
      "org.apache.spark" %% "spark-mllib"                % spark_version,
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % spark_version,
      "org.apache.spark" %% "spark-hive"                 % spark_version,
      "org.apache.spark" %% "spark-yarn"                 % spark_version
    )

    val excludes = Seq(
      ExclusionRule("org.slf4j", "slf4j-log4j12")
    )

    val overrides = Seq()
  }

}
