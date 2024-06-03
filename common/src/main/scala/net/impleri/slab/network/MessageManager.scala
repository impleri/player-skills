package net.impleri.slab.network

trait MessageManager {
  protected def network: Network

  protected def registerFactoryToClient[M <: ClientboundMessage, T <: MessageFactory[M]](factory: T): Unit = {
    val messageType: MessageType.Vanilla = network.registerMessageToClient(factory.name, factory.receive)

    factory.setMessageType(messageType)
  }

  protected def registerFactoryToServer[M <: ServerboundMessage, T <: MessageFactory[M]](factory: T): Unit = {
    val messageType: MessageType.Vanilla = network.registerMessageToServer(factory.name, factory.receive)

    factory.setMessageType(messageType)
  }
}
