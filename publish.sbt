ThisBuild / organization := "io.github.amerousful"
ThisBuild / scalaVersion := "2.13.15"
ThisBuild / versionScheme := Some("pvp")

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/Amerousful/gatling-kafka"),
    "scm:git:git://github.com/Amerousful/gatling-kafka.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "Amerousful",
    name  = "Pavel Bairov",
    email = "amerousful@gmail.com",
    url   = url("https://github.com/Amerousful")
  )
)

ThisBuild / description := "Gatling Kafka Plugin"
ThisBuild / licenses := List("The MIT License (MIT)" -> new URL("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/Amerousful/gatling-kafka"))

ThisBuild / crossPaths := false

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
ThisBuild / publishMavenStyle := true