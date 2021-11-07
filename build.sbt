ThisBuild / scalaVersion := "3.1.0"
ThisBuild / organization := "com.github.esgott"

lazy val root = (project in file("."))
  .settings(
    name := "measure-cpu",
  )
