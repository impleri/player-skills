package net.impleri.playerskills.facades.minecraft

import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.BaseS2CMessage
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.player.{Player => MinecraftPlayer}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionHand

import java.util.UUID
import scala.jdk.CollectionConverters._

case class Player[T <: MinecraftPlayer](private val player: T) extends Entity(player) {
  val isClient: Boolean = player.isInstanceOf[LocalPlayer]

  val isClientSide: Boolean = player.getLevel.isClientSide

  val isServer: Boolean = player.isInstanceOf[ServerPlayer]

  lazy val uuid: UUID = player.getUUID

  val server: Server = Server(player.getServer)

  private def toItemMap(values: NonNullList[ItemStack]): Map[Int, Item] = {
    values.asScala
      .map(Item(_))
      .view
      .zipWithIndex
      .map(_.swap)
      .toMap
  }

  val armor: Map[Int, Item] = toItemMap(player.getInventory.armor)

  def emptyArmor(slot: Int): Unit = player.getInventory.armor.set(slot, Item.DEFAULT_ITEM.getStack)

  val inventory: Map[Int, Item] = toItemMap(player.getInventory.items)

  def toss(item: Item): Unit = player.drop(item.getStack, true)

  val offHand: Map[Int, Item] = toItemMap(player.getInventory.offhand)

  def emptyOffHand(slot: Int): Unit = player.getInventory.offhand.set(slot, Item.DEFAULT_ITEM.getStack)

  private def getServerConnection: Option[ServerGamePacketListenerImpl] = {
    if (isServer) {
      Option(player
        .asInstanceOf[ServerPlayer]
        .connection,
      )
    } else {
      None
    }
  }

  def getItemInHand(hand: InteractionHand): Item = {
    Item(player.getItemInHand(hand))
  }

  def getItemInMainHand: Item = {
    Item(player.getMainHandItem)
  }

  def putInInventory(item: Item): Unit = player.getInventory.placeItemBackInInventory(item.getStack)

  def sendMessage(message: Component, notifyPlayer: Boolean = true): Unit = {
    if (isServer && !isEmpty) {
      player.asInstanceOf[ServerPlayer].sendSystemMessage(message, notifyPlayer)
    }
  }

  def sendMessage(message: BaseS2CMessage): Unit = {
    if (isServer && !isEmpty) {
      message.sendTo(player.asInstanceOf[ServerPlayer])
    }
  }

  def sendMessage(message: BaseC2SMessage): Unit = {
    if (isClient) {
      message.sendToServer()
    }
  }

  private def sendPacket(packet: Packet[_]): Unit = getServerConnection.foreach(_.send(packet))

  def sendEmptyContainerSlot(menu: AbstractContainerMenu): Unit = {
    sendPacket(
      new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY),
    )
  }
}
