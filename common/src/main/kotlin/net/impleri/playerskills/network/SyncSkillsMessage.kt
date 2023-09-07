package net.impleri.playerskills.network

import com.google.common.collect.ImmutableList
import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import java.util.UUID

class SyncSkillsMessage : BaseS2CMessage {
  private val playerId: UUID
  private val skills: MutableList<Skill<*>>
  private val force: Boolean

  internal constructor(buffer: FriendlyByteBuf) {
    playerId = buffer.readUUID()
    force = buffer.readBoolean()
    val size = buffer.readInt()
    skills = ArrayList(size)
    PlayerSkillsLogger.SKILLS.debug("Received skill sync of $size skills for $playerId")
    for (i in 0 until size) {
      val stringSize = buffer.readInt()
      val string = buffer.readUtf(stringSize)

      SkillType.unserializeFromString(string)?.let { skills.add(it) }
    }
  }

  constructor(player: Player, skills: MutableList<Skill<*>>, force: Boolean) {
    playerId = player.uuid
    this.skills = skills
    this.force = force
  }

  override fun getType(): MessageType {
    return Manager.SYNC_SKILLS
  }

  override fun write(buffer: FriendlyByteBuf) {
    buffer.writeUUID(playerId)
    buffer.writeBoolean(force)
    val size = skills.size
    buffer.writeInt(size)
    for (skill in skills) {
      val string = SkillType.serializeToString(skill)
      val stringSize = string.length
      buffer.writeInt(stringSize)
      buffer.writeUtf(string, stringSize)
    }
    PlayerSkillsLogger.SKILLS.debug("Sending skill sync of $size skills for $playerId")
  }

  override fun handle(context: NetworkManager.PacketContext) {
    PlayerSkillsClient.syncFromServer(ImmutableList.copyOf(skills), force)
  }
}
