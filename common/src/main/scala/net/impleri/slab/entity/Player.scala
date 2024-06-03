package net.impleri.slab.entity

import net.impleri.slab.chat.Message
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.item.Item
import net.impleri.slab.menu.ContainerMenu
import net.impleri.slab.network.ClientboundMessage
import net.impleri.slab.network.ServerboundMessage
import net.impleri.slab.server.Server
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.NonNullList
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.player.{Player => McPlayer}

import java.util.UUID
import scala.jdk.CollectionConverters._

case class Player[T <: Player.Vanilla](override val underlying: T) extends Entity[T](underlying) {
  val handle: String = underlying.getName.getString

  val isClient: Boolean = underlying.isInstanceOf[Player.VanillaLocal]

  val isClientSide: Boolean = underlying.getLevel.isClientSide

  val isServer: Boolean = underlying.isInstanceOf[Player.VanillaServer]

  lazy val uuid: UUID = underlying.getUUID

  val server: Server = Server(underlying.getServer)

  private def toItemMap(values: NonNullList[Item.VanillaStack]): Map[Int, Item] = {
    values.asScala
      .map(Item(_))
      .view
      .zipWithIndex
      .filterNot(_._1.isDefault)
      .map(_.swap)
      .toMap
  }

  val armor: Map[Int, Item] = toItemMap(underlying.getInventory.armor)

  def emptyArmor(slot: Int): Unit = underlying.getInventory.armor.set(slot, Item.DEFAULT_ITEM.getStack)

  val inventory: Map[Int, Item] = toItemMap(underlying.getInventory.items)

  def toss(item: Item): Unit = underlying.drop(item.getStack, true)

  val offHand: Map[Int, Item] = toItemMap(underlying.getInventory.offhand)

  def emptyOffHand(slot: Int): Unit = underlying.getInventory.offhand.set(slot, Item.DEFAULT_ITEM.getStack)

  private def getServerConnection: Option[ServerGamePacketListenerImpl] = {
    if (isServer) {
      Option(underlying
        .asInstanceOf[ServerPlayer]
        .connection,
      )
    } else {
      None
    }
  }

  def getItemInHand(hand: Hand): Option[Item] = {
    Option(underlying.getItemInHand(hand.underlying)).map(Item(_)).filterNot(_.isDefault)
  }

  def getItemInMainHand: Option[Item] = {
    Option(underlying.getMainHandItem).map(Item(_)).filterNot(_.isDefault)
  }

  def putInInventory(item: Item): Unit = underlying.getInventory.placeItemBackInInventory(item.getStack)

  def sendMessage(message: Message[_], notifyPlayer: Boolean = true): Unit = {
    if (isServer && !isEmpty) {
      underlying.asInstanceOf[ServerPlayer].sendSystemMessage(message.output, notifyPlayer)
    }
  }

  def sendMessage(message: ClientboundMessage): Unit = {
    if (isServer && !isEmpty) {
      message.sendTo(underlying.asInstanceOf[ServerPlayer])
    }
  }

  def sendMessage(message: ServerboundMessage): Unit = {
    if (isClient) {
      message.sendToServer()
    }
  }

  private def sendPacket(packet: Packet[_]): Unit = getServerConnection.foreach(_.send(packet))

  def sendEmptyContainerSlot(menu: ContainerMenu.Any): Unit = {
    sendPacket(
      new ClientboundContainerSetSlotPacket(menu.getId, menu.getNextStateId, 0, Item.EMPTY_STACK),
    )
  }
}

object Player {
  type Vanilla = McPlayer
  type VanillaLocal = LocalPlayer
  type VanillaServer = ServerPlayer

  type Any = Player[_]
  type Local = Player[VanillaLocal]
  type Server = Player[VanillaServer]


  def fromVanilla(underlying: McPlayer): Option[Player.Any] = Option(underlying).map(Player(_))
}
