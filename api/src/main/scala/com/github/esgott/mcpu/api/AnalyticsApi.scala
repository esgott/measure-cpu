package com.github.esgott.mcpu.api

import com.github.esgott.mcpu.api.ApiError.NotFound
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object AnalyticsApi {

  private val baseEndpoint =
    endpoint
      .errorOut(
        oneOf[ApiError](
          oneOfMapping(StatusCode.NotFound, jsonBody[NotFound])
        )
      )

  lazy val recentCpuUsage: Endpoint[TimeRange, ApiError, CpuReport, Any] =
    baseEndpoint
      .in("analytics" / "recent")
      .in(jsonBody[TimeRange])
      .out(jsonBody[CpuReport])

  lazy val suspiciousEvents: Endpoint[TimeRange, ApiError, SuspiciousEvents, Any] =
    baseEndpoint
      .in("analytics" / "suspicious")
      .in(jsonBody[TimeRange])
      .out(jsonBody[SuspiciousEvents])

}
