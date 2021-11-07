package com.github.esgott.mcpu.server

import com.github.esgott.mcpu.api.{ClientEvent, Header}

case class MessageBusEvent(
    header: Header,
    event: ClientEvent
)
