package com.github.esgott.mcpu.server

import com.github.esgott.mcpu.api.{ClientEvent, Header}

trait Ingestion[F[_]] {
  def ingest(header: Header, event: ClientEvent): F[Unit]
}

object Ingestion {

  def apply[F[_]: Ingestion]: Ingestion[F] = implicitly[Ingestion[F]]

  def ingestion[F[_]: MessageBus]: Ingestion[F] =
    new Ingestion[F] {

      override def ingest(header: Header, event: ClientEvent): F[Unit] =
        MessageBus[F].send(header.clientId, MessageBusEvent(header, event))

    }

}
