import sbt._

object Dependencies {

  private def scalaDep(org: String, prefix: String, version: String): String => ModuleID =
    suffix => org %% depName(prefix, suffix) % version

  private def depName(prefix: String, suffix: String) =
    if (suffix.isEmpty) prefix
    else if (prefix.isEmpty) suffix
    else s"$prefix-$suffix"

  private val algebird = scalaDep("com.twitter", "algebird", "0.13.9")
  private val circe    = scalaDep("io.circe", "circe", "0.14.1")
  private val decline  = scalaDep("com.monovore", "decline", "1.0.0")
  private val fs2      = scalaDep("co.fs2", "fs2", "2.5.10")
  private val tapir    = scalaDep("com.softwaremill.sttp.tapir", "tapir", "0.18.3")

  private val sttpClient =
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats-ce2" % "3.3.16"

  val api = Seq(
    circe("core"),
    circe("generic"),
    circe("generic-extras"),
    tapir("core"),
    tapir("json-circe")
  )

  val client = Seq(
    decline("effect"),
    fs2("core"),
    sttpClient,
    tapir("sttp-client")
  )

  val server = Seq(
    algebird("core"),
    decline("effect"),
    tapir("http4s-server")
  )

}
