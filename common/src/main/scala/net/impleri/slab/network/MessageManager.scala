package net.impleri.slab.network

import scala.util.chaining.scalaUtilChainingOps

trait MessageManager {
  protected def network: Network

  private def withBuffer[T](f: FriendlyBuffer => Option[T])(buffer: FriendlyBuffer.Vanilla): T =
    Option(buffer)
      .map(FriendlyBuffer(_))
      .flatMap(f)
      .getOrElse(null.asInstanceOf[T])

  protected def registerFactoryToClient[
    M <: ClientboundMessage,
    T <: MessageFactory[M],
  ](factory: T): Unit =
      network.registerMessageToClient(factory.name, withBuffer(factory.receive))
        .tap(factory.setMessageType)

  protected def registerFactoryToServer[
    M <: ServerboundMessage,
    T <: MessageFactory[M],
  ](factory: T): Unit =
      network.registerMessageToServer(factory.name, withBuffer(factory.receive))
        .pipe(factory.setMessageType)
}
