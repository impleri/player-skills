package net.impleri.slab.entity

import net.impleri.slab.chat.Message
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.item.Item
import net.impleri.slab.menu.ContainerMenu
import net.impleri.slab.network.ClientboundMessage
import net.impleri.slab.server.Server
import net.minecraft.core.NonNullList
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.world.entity.player.{Player => McPlayer}

import java.util.UUID
import scala.jdk.CollectionConverters._

class Player(override val underlying: Player.Vanilla)
    extends Entity[Player.Vanilla](underlying) {
  val handle: String = underlying.getName.getString

  val isClient: Boolean = underlying.getLevel.isClientSide

  val isServer: Boolean = !isClient && underlying.isInstanceOf[ServerPlayer]

  lazy val uuid: UUID = underlying.getUUID

  val server: Option[Server] = Option(underlying.getServer).map(Server(_))

  private def toItemMap(
    values: NonNullList[Item.VanillaStack],
  ): Map[Int, Item] = {
    values.asScala
      .map(Item(_))
      .view
      .zipWithIndex
      .filterNot(_._1.isDefault)
      .map(_.swap)
      .toMap
  }

  val armor: Map[Int, Item] = toItemMap(underlying.getInventory.armor)

  def emptyArmor(slot: Int): Unit =
    underlying.getInventory.armor.set(slot, Item.DEFAULT_ITEM.getStack)

  val inventory: Map[Int, Item] = toItemMap(underlying.getInventory.items)

  def toss(item: Item): Unit = underlying.drop(item.getStack, true)

  val offHand: Map[Int, Item] = toItemMap(underlying.getInventory.offhand)

  def emptyOffHand(slot: Int): Unit =
    underlying.getInventory.offhand.set(slot, Item.DEFAULT_ITEM.getStack)

  private def getServerConnection: Option[ServerPlayerConnection] =
    Option(underlying)
      .filter(_ => isServer)
      .map(_.asInstanceOf[ServerPlayer])
      .map(_.connection)

  def getItemInHand(hand: Hand): Option[Item] =
    Option(underlying.getItemInHand(hand.underlying))
      .map(Item(_))
      .filterNot(_.isDefault)

  def getItemInMainHand: Option[Item] =
    Option(underlying.getMainHandItem)
      .map(Item(_))
      .filterNot(_.isDefault)

  def putInInventory(item: Item): Unit =
    underlying.getInventory.placeItemBackInInventory(item.getStack)

  def sendMessage(message: Message[_], notifyPlayer: Boolean = true): Unit =
    if (isServer && !isEmpty) {
      underlying
        .asInstanceOf[ServerPlayer]
        .sendSystemMessage(message.output, notifyPlayer)
    }

  def sendMessage(message: ClientboundMessage): Unit =
    if (isServer && !isEmpty) {
      message.sendTo(underlying.asInstanceOf[ServerPlayer])
    }

  private def sendPacket(packet: Packet[_]): Unit =
    getServerConnection.foreach(_.send(packet))

  def sendEmptyContainerSlot(menu: ContainerMenu.Any): Unit =
    sendPacket(
      new ClientboundContainerSetSlotPacket(
        menu.getId,
        menu.getNextStateId,
        0,
        Item.EMPTY_STACK,
      ),
    )
}

object Player {
  type Vanilla = McPlayer

  def apply(underlying: Vanilla): Player = new Player(underlying)

  def fromVanilla(underlying: McPlayer): Option[Player] =
    Option(underlying).map(new Player(_))
}
