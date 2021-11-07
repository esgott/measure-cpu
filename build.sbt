ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "com.github.esgott"

ThisBuild / scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-Ywarn-dead-code",
  "-Ywarn-unused:params"
)

lazy val root = (project in file("."))
  .aggregate(
    api,
    client,
    server
  )

lazy val api = (project in file("api"))
  .settings(
    name := "measure-cpu-api",
    libraryDependencies ++= Dependencies.api
  )

lazy val client = (project in file("client"))
  .dependsOn(api)
  .settings(
    name := "measure-cpu-client",
    libraryDependencies ++= Dependencies.client
  )

lazy val server = (project in file("server"))
  .dependsOn(api)
  .settings(
    name := "measure-cpu-server",
    libraryDependencies ++= Dependencies.server,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
