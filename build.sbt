name := "kafka-simple-webhooks"

version := "0.4"

scalaVersion := "2.12.7"

scalacOptions := Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.google.inject" % "guice" % "4.1.0",
  "org.apache.kafka" % "kafka-streams" % "1.1.1",
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.9",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)

enablePlugins(JavaAppPackaging)

dockerRepository := Some("graphpathai")

dockerUpdateLatest := true

import com.typesafe.sbt.packager.docker._

dockerCommands := Seq(
  Cmd("FROM", "openjdk:8u171-jre-alpine3.8"),
  Cmd("WORKDIR", "/opt/docker"),
  Cmd("USER", "daemon"),
  ExecCmd("ENTRYPOINT", "java", "-Xms32m", "-Xmx128m", "-cp", "lib/*", "com.graphpathai.Main"),
  Cmd("ADD", "--chown=daemon:daemon opt /opt")
)
