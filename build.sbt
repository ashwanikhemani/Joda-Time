name := "joda-time"

scalaVersion := "2.10.3"

organization := "joda-time"

version := "2.9.9"

publishMavenStyle := true

crossPaths := false

autoScalaLibrary := false

libraryDependencies ++= Seq("junit" % "junit" % "4.12" % "test",
  "org.joda" % "joda-convert" % "1.2" % "compile")

