import sbt.Keys.scalacOptions
import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
// global
scalaVersion in Global := "2.12.9"
organization in Global := "me.ooon"

scalacOptions in Global ++= Seq("-unchecked", "-deprecation", "-feature")
resolvers in Global += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
//resolvers in Global += Resolver.url("ooon ivy repo", url("https://repo.ooon.me/release"))(Resolver.ivyStylePatterns)
externalResolvers in Global := Resolver.combineDefaultResolvers(resolvers.value.toVector,
                                                                mavenCentral = true)

libraryDependencies in Global ++= Seq(orison, scalatest)
libraryDependencies in Global ++= log
excludeDependencies in Global ++= excludes
dependencyOverrides in Global ++= overrides

cancelable in Global := true
//

lazy val root = (project in file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    moduleName          := "SIA",
    name                := "SIA",
    logBuffered in Test := false,
    libraryDependencies ++= Seq(spark, slick, java_mail, akka, breeze).flatten,
    libraryDependencies ++= Seq(os_lib,
                                mysql,
                                nscala,
                                sqlite,
                                leveldb,
                                requests,
                                play_json,
                                delta,
                                squants),
    scalacOptions in (Compile, doc) ++= Seq(
      "-implicits",
      "-groups",
      "-doc-title",
      description.value,
      "-doc-version",
      scalaVersion.value,
      "-sourcepath",
      baseDirectory.in(LocalRootProject).value.getAbsolutePath,
      "-doc-source-url",
      scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala"
    )
  )
  .configs(MultiJvm)
  .dependsOn(messages)

val ROOT = config("root")
lazy val docs = (project in file("docs"))
  .enablePlugins(SiteScaladocPlugin, ParadoxSitePlugin, ParadoxMaterialThemePlugin, GhpagesPlugin)
  .settings(
    moduleName := "docs",
    name       := "SIA - Documents",
    ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox),
    previewLaunchBrowser := false,
    previewFixedPort     := Some(9000),
//    previewFixedIp       := Some("0.0.0.0"),
    ghpagesNoJekyll := true,
    git.remoteRepo  := "git@github.com:zhaihao/SIA.git",
    excludeFilter in ghpagesCleanSite := ((f: File) =>
      (ghpagesRepository.value / "CNAME").getCanonicalPath == f.getCanonicalPath),
    sourceDirectory in Paradox := sourceDirectory.value / "main" / "paradox",
    paradoxProperties in Paradox ++= Map(
      "scaladoc.base_url"   -> "http://SIA.ooon.me/api/",
      "github.base_url"     -> "https://github.com/zhaihao/SIA",
      "snip.build.base_dir" -> baseDirectory.value.getAbsolutePath,
      "snip.github_link"    -> "false"
    ),
    paradoxNavigationDepth in Paradox          := 3,
    sourceDirectory in Paradox in paradoxTheme := sourceDirectory.value / "main" / "paradox" / "_template",
    makeSite                                   := makeSite.dependsOn(paradox in Paradox).value,
    mappings in makeSite in Paradox ++= Seq(file("LICENSE") -> "LICENSE"),
    paradoxMaterialTheme in Paradox ~= {
      _.withColor("red", "teal")
        .withFavicon("assets/favicon.ico")
        .withCopyright("© zhaihao")
        .withRepository(uri("https://github.com/zhaihao/SIA"))
        .withSocial(uri("https://github.com/zhaihao"),
                    uri("https://twitter.com/zhaihaoooon"),
                    uri("https://www.facebook.com/zhaihaome"))
        .withLanguage(java.util.Locale.CHINESE)
        .withCustomStylesheet("assets/custom.css")
    },
    autoAPIMappings := true,
    SiteScaladocPlugin
      .scaladocSettings(ROOT, mappings in (Compile, packageDoc) in root, "api/")
  )

lazy val messages = (project in file("messages"))
  .settings(
    moduleName                 := "messages",
    name                       := "SIA - Messages",
    logBuffered in Test        := false,
    libraryDependencies        += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    PB.protoSources in Compile := Seq(sourceDirectory.value / "main" / "protobuf"),
    PB.targets in Compile := Seq(
      scalapb.gen() -> sourceDirectory.value / "main" / "scala"
    )
  )

lazy val bench = (project in file("bench"))
  .enablePlugins(JmhPlugin)
  .settings(
    moduleName          := "bench",
    name                := "SIA - Bench",
    logBuffered in Test := false
  )
