package net.impleri.playerskills.facades

import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.BaseS2CMessage
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

import java.util.UUID

class MinecraftPlayer[T <: Player](private val player: T) {
  val isClient: Boolean = player.isInstanceOf[LocalPlayer]
  val isServer: Boolean = player.isInstanceOf[ServerPlayer]

  lazy val uuid: UUID = player.getUUID

  lazy val server: MinecraftServer = MinecraftServer(player.getServer)

  lazy val name: String = player.getName.getString

  def sendMessage(message: Component, notifyPlayer: Boolean = true): Unit = {
    if (isServer) {
      player.asInstanceOf[ServerPlayer].sendSystemMessage(message, notifyPlayer)
    }
  }

  def sendMessage(message: BaseS2CMessage): Unit = {
    if (isServer) {
      message.sendTo(player.asInstanceOf[ServerPlayer])
    }
  }

  def sendMessage(message: BaseC2SMessage): Unit = {
    if (isClient) {
      message.sendToServer()
    }
  }
}

object MinecraftPlayer {
  def apply[T <: Player](player: T): MinecraftPlayer[T] = new MinecraftPlayer(player)
}
