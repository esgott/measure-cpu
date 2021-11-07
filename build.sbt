ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "com.github.esgott"

ThisBuild / scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-Ywarn-dead-code",
  "-Ywarn-unused:params"
)

lazy val root = (project in file("."))
  .aggregate(
    api
  )

lazy val api = (project in file("api"))
  .settings(
    name := "measure-cpu-api",
    libraryDependencies ++= Dependencies.api
  )
