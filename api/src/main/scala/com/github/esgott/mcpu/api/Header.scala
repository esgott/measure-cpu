package com.github.esgott.mcpu.api

import com.github.esgott.mcpu.api.Header._
import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain

case class Header(
    clientId: ClientId,
    clientVersion: ClientVersion,
    clientOs: ClientOs,
    osVersion: OsVersion
)

object Header {

  case class ClientId(value: String) extends AnyVal

  object ClientId {
    implicit val clientIdCodec: Codec[String, ClientId, TextPlain] =
      Codec.string.map(ClientId(_))(_.value)
  }

  case class ClientVersion(value: String) extends AnyVal

  object ClientVersion {
    implicit val clientVersionCodec: Codec[String, ClientVersion, TextPlain] =
      Codec.string.map(ClientVersion(_))(_.value)
  }

  case class ClientOs(value: String) extends AnyVal

  object ClientOs {
    implicit val clientOsCodec: Codec[String, ClientOs, TextPlain] =
      Codec.string.map(ClientOs(_))(_.value)
  }

  case class OsVersion(value: String) extends AnyVal

  object OsVersion {
    implicit val osVersionCodec: Codec[String, OsVersion, TextPlain] =
      Codec.string.map(OsVersion(_))(_.value)
  }

}
