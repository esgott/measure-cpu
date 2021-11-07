package com.github.esgott.mcpu.api

import io.circe.generic.JsonCodec

@JsonCodec
case class CpuReport(
    averageCpuUsage: Int,
    maxCpuUsage: Int,
    minCpuUsage: Int
)
