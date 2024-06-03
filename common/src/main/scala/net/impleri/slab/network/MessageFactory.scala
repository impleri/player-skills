package net.impleri.slab.network

trait MessageFactory[T <: NetworkMessage] {
  def name: String

  protected def onReceive: MessageFactory.ReceiveFn[T]

  private var messageType: Option[MessageType] = None

  def setMessageType(newType: MessageType.Vanilla): Unit = {
    messageType = Option(newType).map(MessageType(_))
  }

  def receive(buffer: FriendlyBuffer.Vanilla): T = {
    Option(buffer)
      .map(FriendlyBuffer(_))
      .flatMap(b => messageType.map((b, _)))
      .fold(null.asInstanceOf[T])(t => onReceive(t._1, t._2))
  }

  def createForSend(f: MessageFactory.FactoryFn[T]): Option[T] = messageType.map(f)
}

object MessageFactory {
  type FactoryFn[T <: NetworkMessage] = MessageType => T
  type ReceiveFn[T <: NetworkMessage] = (FriendlyBuffer, MessageType) => T
}
