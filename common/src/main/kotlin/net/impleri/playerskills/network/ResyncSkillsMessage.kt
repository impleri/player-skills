package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.EventHandlers
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import java.util.UUID

class ResyncSkillsMessage : BaseC2SMessage {
  private val playerId: UUID

  internal constructor(buffer: FriendlyByteBuf) {
    playerId = buffer.readUUID()
  }

  constructor(player: Player) {
    playerId = player.uuid
  }

  override fun getType(): MessageType {
    return Manager.RESYNC_SKILLS
  }

  override fun write(buffer: FriendlyByteBuf) {
    buffer.writeUUID(playerId)
  }

  override fun handle(context: NetworkManager.PacketContext) {
    EventHandlers.resync(playerId)
  }
}
