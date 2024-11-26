package net.impleri.slab.network

import net.impleri.slab.entity.Player

import scala.util.chaining.scalaUtilChainingOps

protected trait NetworkMessage {
  protected def messageType: MessageType

  // Action to trigger once the message is received
  protected def onReceive(player: Option[Player] = None): Unit

  // Called to write the message before sending
  protected def onSend(writer: FriendlyBuffer): Unit

  protected def send(buf: FriendlyBuffer.Vanilla): Unit =
    Option(buf)
      .map(FriendlyBuffer(_))
      .foreach(onSend)

  protected def receive(context: Network.Context): Unit = {
    Option(context)
      .map(Context(_))
      .flatMap(_.player)
      .pipe(onReceive)
  }
}
