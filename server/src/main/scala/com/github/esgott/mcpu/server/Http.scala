package com.github.esgott.mcpu.server

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.all._
import com.github.esgott.mcpu.api.Api
import com.github.esgott.mcpu.api.ApiError.NotFound
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext

trait Http[F[_]] {
  def routes: HttpRoutes[F]
}

object Http {

  def apply[F[_]: Http]: Http[F] = implicitly[Http[F]]

  def http[F[_]: Concurrent: ContextShift: Timer: Ingestion: LastSeen]: Http[F] = new Http[F] {

    private val sendEventRoute = Http4sServerInterpreter().toRoutes(Api.sendEvent) {
      case (header, event) =>
        for {
          _      <- LastSeen[F].seen(header.clientId, event)
          result <- Ingestion[F].ingest(header, event)
        } yield result.asRight
    }

    private val lastEventRoute = Http4sServerInterpreter().toRoutes(Api.lastEvent) { header =>
      for {
        result <- LastSeen[F].last(header.clientId)
      } yield result.toRight(NotFound("No events seen yet"))
    }

    override def routes: HttpRoutes[F] =
      sendEventRoute <+> lastEventRoute

  }

  def httpServer[F[_]: ConcurrentEffect: Timer: Http](
      host: String,
      port: Int
  ): Resource[F, Server] =
    BlazeServerBuilder[F](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(Router("/" -> Http[F].routes).orNotFound)
      .resource

}
