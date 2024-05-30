package net.impleri.slab.network

import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.{MessageType => ArchMessageType}
import net.minecraft.network.FriendlyByteBuf

import scala.util.chaining.scalaUtilChainingOps

trait ClientboundMessage extends BaseS2CMessage with NetworkMessage {
  protected def messageType: MessageType

  protected def onReceive: () => Unit

  override def getType: ArchMessageType = messageType.value

  def write(writer: FriendlyBuffer): Unit

  override def write(buf: FriendlyByteBuf): Unit = {
    Option(buf)
      .map(FriendlyBuffer)
      .pipe(write)
  }

  override def handle(context: NetworkManager.PacketContext): Unit = onReceive()
}
