package com.github.esgott.mcpu.server

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.github.esgott.mcpu.api.ClientEvent
import com.github.esgott.mcpu.api.Header.ClientId

trait LastSeen[F[_]] {
  def seen(client: ClientId, event: ClientEvent): F[Unit]
  def last(clientId: ClientId): F[Option[ClientEvent]]
}

object LastSeen {

  def apply[F[_]: LastSeen]: LastSeen[F] = implicitly[LastSeen[F]]

  def lastSeen[F[_]: Sync]: F[LastSeen[F]] =
    for {
      store <- Ref.of[F, Map[ClientId, ClientEvent]](Map.empty)
    } yield new LastSeen[F] {

      override def seen(client: ClientId, event: ClientEvent): F[Unit] =
        store.update(_.updated(client, event))

      override def last(clientId: ClientId): F[Option[ClientEvent]] =
        for {
          map <- store.get
        } yield map.get(clientId)

    }

}
