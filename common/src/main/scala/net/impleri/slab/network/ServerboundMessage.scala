package net.impleri.slab.network

import dev.architectury.networking.simple.BaseC2SMessage

trait ServerboundMessage extends BaseC2SMessage with NetworkMessage {
  override def getType: MessageType.Vanilla = messageType.value

  override def write(buf: FriendlyBuffer.Vanilla): Unit = send(buf)

  override def handle(context: Network.Context): Unit = receive(context)
}

object ServerboundMessage {
  type Vanilla = BaseC2SMessage
}
