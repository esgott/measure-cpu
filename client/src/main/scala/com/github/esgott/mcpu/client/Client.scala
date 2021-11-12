package com.github.esgott.mcpu.client

import java.time.Instant
import scala.concurrent.duration._

import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO}
import cats.syntax.all._
import com.github.esgott.mcpu.api.ClientEvent.CpuMeasurement
import com.github.esgott.mcpu.api.Header.ClientId
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import fs2.Stream
import sttp.client3._
import sttp.model.Uri

object Client
    extends CommandIOApp(
      name = "measure-cpu-client",
      header = "Measure CPU Client"
    ) {

  case class Config(
      clientId: ClientId,
      serverUri: Uri
  )

  private val clientIdOps: Opts[ClientId] =
    Opts
      .option[String](long = "client-id", short = "c", metavar = "client-id", help = "Client ID")
      .map(ClientId.apply)

  private val serverUriOps: Opts[Uri] =
    Opts
      .option[String](long = "server-uri", short = "s", metavar = "server-uri", help = "Server URI")
      .mapValidated(u => Uri.safeApply(u).toValidated.leftMap(NonEmptyList.one))
      .withDefault(uri"http://localhost:8080")

  private val opts: Opts[Config] =
    (clientIdOps, serverUriOps).mapN(Config.apply)

  override def main: Opts[IO[ExitCode]] = opts.map { config =>
    EventSender.eventSender[IO](config.serverUri, config.clientId).use { eventSender =>
      for {
        _ <- Stream
               .awakeEvery[IO](10.seconds)
               .map(_ => CpuMeasurement(Instant.now(), Map("cpu1" -> 10)))
               .evalTap(event => eventSender.send(event))
               .compile
               .drain
      } yield ExitCode.Success
    }
  }

}
