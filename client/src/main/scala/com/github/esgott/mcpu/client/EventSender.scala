package com.github.esgott.mcpu.client

import cats.effect.{Concurrent, ContextShift, Resource}
import cats.syntax.all._
import com.github.esgott.mcpu.api.Header.{ClientId, ClientOs, ClientVersion, OsVersion}
import com.github.esgott.mcpu.api.{Api, ClientEvent, Header}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter

trait EventSender[F[_]] {
  def send(event: ClientEvent): F[Unit]
}

object EventSender {

  def apply[F[_]: EventSender]: EventSender[F] = implicitly[EventSender[F]]

  def eventSender[F[_]: Concurrent: ContextShift](
      baseUri: Uri,
      clientId: ClientId
  ): Resource[F, EventSender[F]] =
    for {
      client <- AsyncHttpClientCatsBackend.resource[F]()
    } yield new EventSender[F] {

      private val header = Header(
        clientId = clientId,
        clientVersion = ClientVersion("1.0"),
        clientOs = ClientOs("linux"),
        osVersion = OsVersion("1.0")
      )

      private val sendEvent =
        SttpClientInterpreter().toRequestThrowErrors(Api.sendEvent, baseUri.some)

      override def send(event: ClientEvent): F[Unit] =
        for {
          response <- client.send(sendEvent(header -> event))
          _        <- new RuntimeException(s"Failed to send request. Response: $response").raiseError
                        .unlessA(response.isSuccess)
        } yield response.body

    }

}
