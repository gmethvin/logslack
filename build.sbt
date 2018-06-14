organization in ThisBuild := "io.methvin"
organizationName in ThisBuild := "Greg Methvin"
startYear in ThisBuild := Some(2018)
licenses in ThisBuild := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))
homepage in ThisBuild := Some(url("https://github.com/gmethvin/logslack"))
scmInfo in ThisBuild := Some(
  ScmInfo(url("https://github.com/gmethvin/logslack"), "scm:git@github.com:gmethvin/logslack.git")
)
developers in ThisBuild := List(
  Developer("gmethvin", "Greg Methvin", "greg@methvin.net", new URL("https://github.com/gmethvin"))
)

scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions in ThisBuild := Seq("2.11.12", scalaVersion.value)

val AkkaVersion = "2.5.12"
val AkkaHttpVersion = "10.1.2"
val LogbackVersion = "1.2.3"
val SprayJsonVersion = "1.3.3"

lazy val `logslack` = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "io.spray" %% "spray-json" % SprayJsonVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )

publishMavenStyle in ThisBuild := true
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

import ReleaseTransformations._
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

scalafmtOnCompile := true
