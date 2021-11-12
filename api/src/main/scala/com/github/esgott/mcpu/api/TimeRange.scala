package com.github.esgott.mcpu.api

import java.time.Instant

import io.circe.generic.JsonCodec

@JsonCodec
case class TimeRange(
    from: Instant,
    until: Instant
)
