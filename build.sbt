name := "play-handlebars"

version := "0.0.1-SNAPSHOT"

sbtPlugin := true

organization := "ca.riedler"

description := "SBT plugin for precompiling Handlebar assets in Play 2.1"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

/// Dependencies

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.7.1" % "test"
)

addSbtPlugin("play" % "sbt-plugin" % "2.1.0")

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/AlexRiedler/play-handlebars</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>https://github.com/AlexRiedler/play-handlebars/blob/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:AlexRiedler/play-handlebars.git</url>
    <connection>scm:git:git@github.com:AlexRiedler/play-handlebars.git</connection>
  </scm>
  <developers>
    <developer>
      <id>AlexRiedler</id>
      <name>Alex Riedler</name>
      <url>https://github.com/Alex Riedler</url>
    </developer>
  </developers>
)
