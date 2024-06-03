package net.impleri.slab.network

import dev.architectury.networking.simple.BaseC2SMessage

trait ServerboundMessage extends BaseC2SMessage with NetworkMessage {
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

object ServerboundMessage {
  type Vanilla = BaseC2SMessage
}
