name := "gatling-kafka"

version := "3.0"

scalaVersion := "2.13.12"

val gatlingVersion = "3.9.5"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core" % gatlingVersion % "provided",
  "io.gatling" % "gatling-core-java" % gatlingVersion % "provided",
  "com.github.spotbugs" % "spotbugs-annotations" % "4.7.3",

  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  "com.typesafe.akka" %% "akka-protobuf-v3" % "2.6.20",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",

  "com.thesamet.scalapb" % "scalapb-runtime_2.13" % "0.11.15",
  "io.confluent" % "kafka-protobuf-serializer" % "7.6.0" % "provided",
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