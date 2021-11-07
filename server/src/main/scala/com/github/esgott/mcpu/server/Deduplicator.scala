package com.github.esgott.mcpu.server

import cats.ApplicativeError
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.github.esgott.mcpu.api.ClientEvent._
import com.github.esgott.mcpu.api.Header.ClientId
import com.twitter.algebird._
import io.circe.syntax._
import fs2.Pipe

import java.time.Instant

trait Deduplicator[F[_]] {
  def check(event: MessageBusEvent): F[Option[MessageBusEvent]]
}

object Deduplicator {

  def apply[F[_]: Deduplicator]: Deduplicator[F] = implicitly[Deduplicator[F]]

  case class UniqueData(time: Instant, client: ClientId, eventType: String)

  object UniqueData {

    def apply[F[_]](
        event: MessageBusEvent
    )(implicit me: ApplicativeError[F, Throwable]): F[UniqueData] = {

      def eventType(event: MessageBusEvent): F[String] =
        event.event.asJson.hcursor.downField("type").as[String].liftTo[F]

      for {
        typ <- eventType(event)
      } yield UniqueData(event.event.time, event.header.clientId, typ)
    }

    implicit val uniqueDataHash: Hash128[UniqueData] =
      Hash128.stringHash.contramap(data => s"${data.time},${data.client.value},${data.eventType}")

  }

  def deduplicator[F[_]: Sync](
      numEntries: Int,
      fpProb: Double,
      defaultWidth: Int
  ): F[Deduplicator[F]] =
    for {
      filter <- Ref.of[F, BF[UniqueData]] {
                  val width     = BloomFilter.optimalWidth(numEntries, fpProb).getOrElse(defaultWidth)
                  val numHashes = BloomFilter.optimalNumHashes(numEntries, width)
                  new BloomFilterMonoid[UniqueData](numHashes, width).create()
                }
    } yield new Deduplicator[F] {

      override def check(event: MessageBusEvent): F[Option[MessageBusEvent]] =
        for {
          unique   <- UniqueData(event)
          contains <- filter.modify { bf =>
                        val contains = bf.maybeContains(unique)
                        val updated  = if (contains) bf else bf + unique
                        (updated, contains)
                      }
        } yield if (contains) None else event.some

    }

  def pipe[F[_]: Deduplicator]: Pipe[F, MessageBusEvent, MessageBusEvent] = stream =>
    stream
      .evalMap(Deduplicator[F].check)
      .collect { case Some(event) => event }

}
