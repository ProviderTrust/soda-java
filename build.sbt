import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

mimaDefaultSettings

scalaVersion := "2.10.5"

organization := "com.i2rd.socrata"

name := "soda-api-java"

// remember to update the README when you change this to a new release
version := "0.9.13"

previousArtifact := Some("com.i2rd.socrata" % "soda-api-java" % "0.9.11")

javacOptions in compile ++= Seq("-g", "-Xlint:deprecation", "-Xlint:unchecked", "-target", "1.8", "-source", "1.8")

autoScalaLibrary := false

crossPaths := false

libraryDependencies ++= Seq(
  "com.opencsv" % "opencsv" % "3.10",
  "joda-time" % "joda-time" % "2.1",
  "com.sun.jersey" % "jersey-bundle" % "1.9.1",
  "com.sun.jersey.contribs" % "jersey-multipart" % "1.9.1",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.6",
  "com.fasterxml.jackson.module" % "jackson-module-jaxb-annotations" % "2.8.6",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.8.6",
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % "2.8.6",
  "com.google.guava" % "guava" % "22.0",
  "org.apache.commons" % "commons-lang3" % "3.6",
  "com.google.code.findbugs" % "annotations" % "2.0.1",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "1.3.2",
  "com.novocode" % "junit-interface" % "0.9" % "test"
)

parallelExecution in Test := false

logBuffered in Test := false

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.JUnit, "-v")
)
