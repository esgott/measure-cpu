package com.github.esgott.mcpu.server

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import fs2.Pipe

trait EventStore[F[_]] {
  def store(event: MessageBusEvent): F[Unit]
}

object EventStore {

  def apply[F[_]: EventStore]: EventStore[F] = implicitly[EventStore[F]]

  def eventStore[F[_]: Sync]: F[EventStore[F]] =
    for {
      db <- Ref.of[F, List[MessageBusEvent]](List.empty)
    } yield new EventStore[F] {

      override def store(event: MessageBusEvent): F[Unit] =
        db.update(event :: _)

    }

  def pipe[F[_]: EventStore]: Pipe[F, MessageBusEvent, Unit] = stream =>
    stream.evalMap(EventStore[F].store)

}
