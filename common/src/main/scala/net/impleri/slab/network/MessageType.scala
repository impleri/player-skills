package net.impleri.slab.network

import dev.architectury.networking.simple.{MessageType => ArchMessageType}

case class MessageType(private val underlying: ArchMessageType) {
  def value: ArchMessageType = underlying
}
