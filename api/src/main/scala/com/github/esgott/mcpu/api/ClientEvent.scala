package com.github.esgott.mcpu.api

import io.circe.Codec
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

import java.time.Instant

sealed trait ClientEvent {
  def time: Instant
}

object ClientEvent {

  @JsonCodec
  case class CpuMeasurement(time: Instant, usage: Map[String, Int]) extends ClientEvent

  @JsonCodec
  case class FileTransferStarted(time: Instant, transferId: String) extends ClientEvent

  @JsonCodec
  case class FileTransferProgress(time: Instant, transferId: String) extends ClientEvent

  @JsonCodec
  case class FileTransferCompleted(time: Instant, transferId: String) extends ClientEvent

  @JsonCodec
  case class AppOpened(time: Instant) extends ClientEvent

  @JsonCodec
  case class AppClosed(time: Instant) extends ClientEvent

  implicit val clientEventCodec: Codec[ClientEvent] = deriveConfiguredCodec

}
