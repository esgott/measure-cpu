import sbt._

object Dependencies {

  private def scalaDep(org: String, prefix: String, version: String): String => ModuleID =
    suffix => org %% depName(prefix, suffix) % version

  private def depName(prefix: String, suffix: String) =
    if (suffix.isEmpty) prefix
    else if (prefix.isEmpty) suffix
    else s"$prefix-$suffix"

  private val circe = scalaDep("io.circe", "circe", "0.14.1")
  private val tapir = scalaDep("com.softwaremill.sttp.tapir", "tapir", "0.18.3")

  val api = Seq(
    circe("core"),
    circe("generic"),
    circe("generic-extras"),
    tapir("core"),
    tapir("json-circe")
  )

}
