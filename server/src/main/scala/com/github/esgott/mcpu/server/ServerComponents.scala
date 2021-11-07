package com.github.esgott.mcpu.server

import cats.effect.{ContextShift, IO, Resource, Timer}
import com.github.esgott.mcpu.server.Server.Config
import org.http4s.server.{Server => HttpServer}

class ServerComponents(implicit
    val messageBus: MessageBus[IO],
    val lastSeen: LastSeen[IO],
    val ingestion: Ingestion[IO],
    val http: Http[IO],
    val httpServer: HttpServer,
    val deduplicator: Deduplicator[IO],
    val store: EventStore[IO]
)

object ServerComponents {

  def apply(
      config: Config
  )(implicit cs: ContextShift[IO], t: Timer[IO]): Resource[IO, ServerComponents] =
    for {
      implicit0(messageBus: MessageBus[IO]) <- eval(MessageBus.messageBus[IO])
      implicit0(lastSeen: LastSeen[IO])     <- eval(LastSeen.lastSeen[IO])
      implicit0(ingestion: Ingestion[IO])   <- pure(Ingestion.ingestion[IO])
      implicit0(http: Http[IO])             <- pure(Http.http[IO])
      implicit0(httpServer: HttpServer)     <- Http.httpServer[IO](config.host, config.port)

      implicit0(deduplicator: Deduplicator[IO]) <-
        eval(
          Deduplicator.deduplicator[IO](config.bfNumEntries, config.bfFpProb, config.bfDefaultWidth)
        )

      implicit0(store: EventStore[IO])          <- eval(EventStore.eventStore[IO])
    } yield new ServerComponents

  private def eval[T](io: IO[T]): Resource[IO, T] =
    Resource.eval(io)

  private def pure[T](t: T): Resource[IO, T] =
    Resource.pure[IO, T](t)

}
