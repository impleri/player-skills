package net.impleri.slab.network

import net.impleri.slab.logging.Logger

trait MessageTypeState[T <: NetworkMessage] {
  protected def logger: Logger

  protected var messageType: Option[MessageType] = None

  protected def messageName: String = messageType.fold("[None]")(_.toString)

  def setMessageType(newType: MessageType.Vanilla): Unit =
    messageType = Option(newType).map(MessageType(_))

  def createForSend(f: MessageType => T): Option[T] =
    for {
      m <- messageType
    } yield {
      logger.debug(s"Sending $messageName message")

      f(m)
    }
}

trait MessageFactory[T <: NetworkMessage] extends MessageTypeState[T] {
  def name: String

  protected def logger: Logger

  // abstract method to implement message parsing
  protected def parse(buffer: FriendlyBuffer, messageType: MessageType): Option[T]

  // Reconstructs message from buffer once set across the network
  def receive(buffer: FriendlyBuffer): Option[T] =
    for {
      t <- messageType
      _ = logger.debug(s"Received $messageName message")
      if buffer.nonEmpty
      m <- parse(buffer, t)
    } yield {
      logger.debug(s"Parsed $messageName message successfully")

      m
    }
}
