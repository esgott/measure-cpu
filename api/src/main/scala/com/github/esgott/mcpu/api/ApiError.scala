package com.github.esgott.mcpu.api

import io.circe.generic.JsonCodec

sealed trait ApiError

object ApiError {

  @JsonCodec
  case class NotFound(msg: String) extends ApiError

}
