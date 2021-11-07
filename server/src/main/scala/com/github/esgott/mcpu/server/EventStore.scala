package com.github.esgott.mcpu.server

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.github.esgott.mcpu.api.TimeRange
import fs2.Pipe

trait EventStore[F[_]] {
  def store(event: MessageBusEvent): F[Unit]
  def getRange(range: TimeRange): F[List[MessageBusEvent]]
}

object EventStore {

  def apply[F[_]: EventStore]: EventStore[F] = implicitly[EventStore[F]]

  def eventStore[F[_]: Sync]: F[EventStore[F]] =
    for {
      db <- Ref.of[F, List[MessageBusEvent]](List.empty)
    } yield new EventStore[F] {

      override def store(event: MessageBusEvent): F[Unit] =
        db.update(event :: _)

      override def getRange(range: TimeRange): F[List[MessageBusEvent]] =
        for {
          events <- db.get
        } yield events
          .filter(_.event.time.isAfter(range.from))
          .filter(_.event.time.isBefore(range.until))

    }

  def pipe[F[_]: EventStore]: Pipe[F, MessageBusEvent, Unit] = stream =>
    stream.evalMap(EventStore[F].store)

}
