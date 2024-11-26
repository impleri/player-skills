package net.impleri.slab.network

import dev.architectury.networking.simple.BaseS2CMessage

trait ClientboundMessage extends BaseS2CMessage with NetworkMessage {
  override def getType: MessageType.Vanilla = messageType.value

  override def write(buf: FriendlyBuffer.Vanilla): Unit = send(buf)

  override def handle(context: Network.Context): Unit = receive(context)
}

object ClientboundMessage {
  type Vanilla = BaseS2CMessage
}
