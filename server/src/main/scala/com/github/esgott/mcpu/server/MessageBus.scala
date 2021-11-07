package com.github.esgott.mcpu.server

import cats.effect.Concurrent
import cats.syntax.all._
import com.github.esgott.mcpu.api.Header.ClientId
import fs2.Stream
import fs2.concurrent.Queue

trait MessageBus[F[_]] {
  def send(key: ClientId, value: MessageBusEvent): F[Unit]
  def receive: Stream[F, (ClientId, MessageBusEvent)]
}

object MessageBus {

  def apply[F[_]: MessageBus]: MessageBus[F] = implicitly[MessageBus[F]]

  def messageBus[F[_]: Concurrent]: F[MessageBus[F]] =
    for {
      queue <- Queue.unbounded[F, (ClientId, MessageBusEvent)]
    } yield new MessageBus[F] {

      override def send(key: ClientId, value: MessageBusEvent): F[Unit] =
        queue.enqueue1(key -> value)

      override def receive: Stream[F, (ClientId, MessageBusEvent)] =
        queue.dequeue

    }

}
