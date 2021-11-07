package com.github.esgott.mcpu.server

import cats.Monad
import cats.data.{EitherT, NonEmptyList}
import cats.syntax.all._
import com.github.esgott.mcpu.api.ClientEvent.CpuMeasurement
import com.github.esgott.mcpu.api.Header.ClientId
import com.github.esgott.mcpu.api.SuspiciousEvents.SuspiciousEvent
import com.github.esgott.mcpu.api._

import java.time.{Duration, Instant}

trait Analytics[F[_]] {
  def recentCpuUsage(range: TimeRange): EitherT[F, ApiError, CpuReport]
  def suspiciousEvents(range: TimeRange): EitherT[F, ApiError, SuspiciousEvents]
}

object Analytics {

  def apply[F[_]: Analytics]: Analytics[F] = implicitly[Analytics[F]]

  case class ClientCpuMeasurement(
      clientId: ClientId,
      time: Instant,
      avgUsage: Int,
      usage: Map[String, Int]
  ) {
    def toEvent: CpuMeasurement = CpuMeasurement(time, usage)
  }

  def analytics[F[_]: Monad: EventStore]: Analytics[F] =
    new Analytics[F] {

      override def recentCpuUsage(range: TimeRange): EitherT[F, ApiError, CpuReport] =
        for {
          events <- readEvents(range)

          measurements = events
                           .map(_.event)
                           .collect { case CpuMeasurement(_, usage) => usage }

        } yield CpuReport(
          averageCpuUsage = measurements.flatMap(_.values).sum / measurements.size,
          maxCpuUsage = measurements.map(_.values.max).max,
          minCpuUsage = measurements.map(_.values.min).min
        )

      override def suspiciousEvents(range: TimeRange): EitherT[F, ApiError, SuspiciousEvents] =
        for {
          events <- readEvents(range)

          measurements =
            events
              .collect {
                case MessageBusEvent(Header(clientId, _, _, _), CpuMeasurement(time, usage))
                    if usage.nonEmpty =>
                  ClientCpuMeasurement(clientId, time, usage.values.sum / usage.size, usage)
              }

          localMaximums = measurements
                            .sliding(3)
                            .flatMap {
                              case first :: second :: third :: Nil
                                  if first.avgUsage < second.avgUsage && second.avgUsage > third.avgUsage =>
                                List(second)
                              case _ =>
                                List.empty
                            }
                            .toList

        } yield SuspiciousEvents(
          events = localMaximums.flatMap { measurement =>
            findClosestEvent(events, measurement)
              .map(SuspiciousEvent(measurement.toEvent, _))
          }
        )

      private def readEvents(
          range: TimeRange
      ): EitherT[F, ApiError, NonEmptyList[MessageBusEvent]] =
        for {
          events <- EitherT.liftF(EventStore[F].getRange(range))

          nel <- EitherT.fromEither[F](
                   NonEmptyList
                     .fromList(events)
                     .toRight(ApiError.NotFound("No events in time range"))
                     .leftWiden[ApiError]
                 )
        } yield nel

      private def findClosestEvent(
          events: NonEmptyList[MessageBusEvent],
          measurement: ClientCpuMeasurement
      ): Option[ClientEvent] =
        events
          .filter(_.header.clientId == measurement.clientId)
          .flatMap {
            case MessageBusEvent(_, CpuMeasurement(_, _)) => none
            case other                                    => other.some
          }
          .minByOption(event =>
            math.abs(Duration.between(event.event.time, measurement.time).toNanos)
          )
          .map(_.event)

    }

}
