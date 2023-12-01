import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.cs434"
ThisBuild / organizationName := "cs434"

name := "sortnet"
version := "0.1.0"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    // log4j2 scala (http://logging.apache.org/log4j/scala/index.html)
    "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0",
    "org.apache.logging.log4j" % "log4j-core" % "2.13.0" % Runtime,
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "junit" % "junit" % "4.10" % "test",

  ),
)

lazy val root = (project in file("."))
  .settings(
    name := "sortnet",
    libraryDependencies += munit % Test
  )
  .settings(commonSettings)
  .aggregate(core, network, master, worker)
  

// core project
lazy val core = (project in file("./core"))
  .settings(
    name := "core",
  )
  .settings(commonSettings)

// network project
lazy val network = (project in file("./network"))
  .settings(
    name := "network",
  )
  .settings(commonSettings)
  .dependsOn(core)

// master project
lazy val master = (project in file("./master"))
  .settings(
    name := "master",
  )
  .settings(commonSettings)
  .dependsOn(core)
  .dependsOn(network)

// worker project
lazy val worker = (project in file("./worker"))
  .settings(
    name := "worker",
  )
  .settings(commonSettings)
  .dependsOn(core)
  .dependsOn(network)