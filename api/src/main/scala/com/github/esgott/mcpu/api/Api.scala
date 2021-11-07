package com.github.esgott.mcpu.api

import com.github.esgott.mcpu.api.ApiError._
import com.github.esgott.mcpu.api.Header._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object Api {

  private val eventHeaders: EndpointInput[Header] =
    header[ClientId]("Client-Id")
      .and(header[ClientVersion]("Client-Version"))
      .and(header[ClientOs]("Client-Os"))
      .and(header[OsVersion]("Os-Version"))
      .mapTo[Header]

  private val baseEndpoint =
    endpoint
      .in(eventHeaders)
      .errorOut(
        oneOf[ApiError](
          oneOfMapping(StatusCode.NotFound, jsonBody[NotFound])
        )
      )

  lazy val sendEvent: Endpoint[(Header, ClientEvent), ApiError, Unit, Any] =
    baseEndpoint.post
      .in("event")
      .in(jsonBody[ClientEvent])

  lazy val lastEvent: Endpoint[Header, ApiError, ClientEvent, Any] =
    baseEndpoint.get
      .in("event")
      .out(jsonBody[ClientEvent])

}
