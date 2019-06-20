import sbt._
import sbtrelease.ReleaseStateTransformations._

import scala.language.postfixOps
import scala.sys.process._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  //  commitReleaseVersion,
  //  tagRelease,
  //  publishArtifacts,
  //  runPublishLocal,
  runPublish,
  chmodStep,
  setNextVersion,
  commitNextVersion
  //  pushChanges
)

releaseCrossBuild           := true
releaseIgnoreUntrackedFiles := true

releaseTagComment    := s"chore: releasing ${(version in ThisBuild).value}"
releaseCommitMessage := s"chore: prepare version ${(version in ThisBuild).value}"

lazy val runPublishLocal: ReleaseStep = ReleaseStep(
  action = {
    st: State =>
      val extracted = Project.extract(st)
      val ref       = extracted.get(thisProjectRef)
      val _st       = extracted.runAggregated(publishLocal in Global in ref, st)
      _st
  },
  enableCrossBuild = true
)

publishTo := Some {
  val key = new java.io.File(sys.env("HOME") + "/.ssh/id_rsa")
  Resolver
    .ssh(
      "my repo",
      "repo.ooon.me",
      29344,
      "/var/lib/docker/volumes/docker_repo/_data/release"
    )(Resolver.ivyStylePatterns)
    .as("root", key)
}

publishMavenStyle := false

lazy val runPublish: ReleaseStep = ReleaseStep(
  action = {
    st: State =>
      val extracted = Project.extract(st)
      val ref       = extracted.get(thisProjectRef)
      val _st       = extracted.runAggregated(publish in Global in ref, st)
      _st
  },
  enableCrossBuild = true
)

lazy val chmodStep: ReleaseStep = ReleaseStep(
  action = {
    st: State =>
      val extracted = Project.extract(st)
      val _st       = extracted.runTask(sh, st)._1
      _st
  },
  enableCrossBuild = false
)

val sh = taskKey[Unit]("shell task")

sh := {
  val log = streams.value.log

  """/Users/zhaihao/code/$name$/chmod.sh""" !

  log.success("set files visible")
}
