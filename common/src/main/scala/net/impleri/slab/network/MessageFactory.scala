package net.impleri.slab.network

import dev.architectury.networking.simple.{MessageType => ArchMessageType}
import net.minecraft.network.FriendlyByteBuf

trait MessageFactory[T <: NetworkMessage] {
  def name: String

  protected def onReceive: MessageFactory.ReceiveFn[T]

  private var messageType: Option[MessageType] = None

  def setMessageType(newType: ArchMessageType): Unit = {
    messageType = Option(newType).map(MessageType)
  }

  def receive(buffer: FriendlyByteBuf): T = {
    Option(buffer)
      .map(FriendlyBuffer)
      .flatMap(b => messageType.map((b, _)))
      .map(t => onReceive(t._1, t._2))
      .orNull
      .asInstanceOf[T]
  }

  def createForSend(f: MessageFactory.FactoryFn[T]): Option[T] = messageType.map(f)
}

object MessageFactory {
  type FactoryFn[T <: NetworkMessage] = MessageType => T
  type ReceiveFn[T <: NetworkMessage] = (FriendlyBuffer, MessageType) => T
}
