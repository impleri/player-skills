package net.impleri.slab.network

import dev.architectury.networking.simple.MessageType

trait MessageManager {
  protected def network: Network

  protected def registerFactoryToClient[M <: NetworkMessage, T <: MessageFactory[M]](factory: T): Unit = {
    val messageType: MessageType = network.registerMessageToClient(factory.name, factory.receive)

    factory.setMessageType(messageType)
  }

  protected def registerFactoryToServer[M <: NetworkMessage, T <: MessageFactory[M]](factory: T): Unit = {
    val messageType: MessageType = network.registerMessageToServer(factory.name, factory.receive)

    factory.setMessageType(messageType)
  }
}
