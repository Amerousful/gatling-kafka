name := "gatling-kafka"

version := "1.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core" % "3.10.3" % "provided",

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