package com.github.esgott.mcpu.api

import io.circe.generic.JsonCodec

import java.time.Instant

@JsonCodec
case class TimeRange(
    from: Instant,
    until: Instant
)
