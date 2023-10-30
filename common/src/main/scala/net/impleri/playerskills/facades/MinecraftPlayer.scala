package net.impleri.playerskills.facades

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

import java.util.UUID

class MinecraftPlayer[T <: Player](private val player: T) {
  val isServer: Boolean = player.isInstanceOf[ServerPlayer]

  lazy val uuid: UUID = player.getUUID

  lazy val server: MinecraftServer = MinecraftServer(player.getServer)

  lazy val name: String = player.getName.getString

  def sendMessage(message: Component, notifyPlayer: Boolean = true): Unit = {
    if (isServer) {
      player.asInstanceOf[ServerPlayer].sendSystemMessage(message, notifyPlayer)
    }
  }
}

object MinecraftPlayer {
  def apply[T <: Player](player: T): MinecraftPlayer[T] = new MinecraftPlayer(player)
}
