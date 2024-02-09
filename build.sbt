name := "gatling-kafka"

version := "2.0"

scalaVersion := "2.13.12"

val gatlingVersion = "3.9.5"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core" % gatlingVersion % "provided",
  "io.gatling" % "gatling-core-java" % gatlingVersion % "provided",
  "com.github.spotbugs" % "spotbugs-annotations" % "4.7.3",

  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  "com.typesafe.akka" %% "akka-protobuf-v3" % "2.6.20",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)