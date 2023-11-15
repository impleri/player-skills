package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.server.PlayerSkillsServer
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID

case class ResyncSkillsMessage(private val playerId: UUID) extends BaseC2SMessage {
  override def getType: MessageType = Manager.RESYNC_SKILLS

  override def write(buffer: FriendlyByteBuf): Unit = buffer.writeUUID(playerId)

  override def handle(context: NetworkManager.PacketContext): Unit = {
    PlayerSkillsServer.STATE.SERVER
      .foreach(PlayerSkillsServer.STATE.EVENT_HANDLERS.resync(playerId, _))
  }
}

object ResyncSkillsMessage {
  def apply(buffer: FriendlyByteBuf): ResyncSkillsMessage = {
    val playerId = buffer.readUUID()

    new ResyncSkillsMessage(playerId)
  }

  def apply(player: MinecraftPlayer[_]): ResyncSkillsMessage = {
    new ResyncSkillsMessage(player.uuid)
  }
}
