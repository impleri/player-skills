package net.impleri.slab.network

import dev.architectury.networking.simple.BaseS2CMessage

trait ClientboundMessage extends BaseS2CMessage with NetworkMessage {
  protected def messageType: MessageType

  protected def onReceive: () => Unit

  override def getType: MessageType.Vanilla = messageType.value

  def write(writer: FriendlyBuffer): Unit

  override def write(buf: FriendlyBuffer.Vanilla): Unit = {
    Option(buf)
      .map(FriendlyBuffer(_))
      .foreach(write)
  }

  override def handle(context: Network.Context): Unit = onReceive()
}

object ClientboundMessage {
  type Vanilla = BaseS2CMessage
}
