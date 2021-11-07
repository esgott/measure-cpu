package com.github.esgott.mcpu.api

import com.github.esgott.mcpu.api.ClientEvent.CpuMeasurement
import com.github.esgott.mcpu.api.SuspiciousEvents._
import io.circe.generic.JsonCodec

@JsonCodec
case class SuspiciousEvents(
    events: List[SuspiciousEvent]
)

object SuspiciousEvents {

  @JsonCodec
  case class SuspiciousEvent(topCpu: CpuMeasurement, closestEvent: ClientEvent)

}
