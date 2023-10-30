package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

class SyncSkillsMessage(
  private val playerId: UUID,
  private val skills: List[Skill[_]],
  private val force: Boolean,
) extends BaseS2CMessage {
  override def getType: MessageType = Manager.SYNC_SKILLS

  override def write(buffer: FriendlyByteBuf): Unit = {
    buffer.writeUUID(playerId)
      .writeBoolean(force)
      .writeInt(skills.size)

    skills.flatMap(SkillType().serialize(_))
      .map(s => (s, s.length))
      .tap(_.map(t => buffer.writeInt(t._2)))
      .tap(_.map(t => buffer.writeUtf(t._1, t._2)))

    PlayerSkillsLogger.SKILLS.debug(s"Sending skill sync of ${skills.size} skills for $playerId")
  }

  override def handle(context: NetworkManager.PacketContext): Unit = PlayerSkillsClient.syncFromServer(skills, force)
}

object SyncSkillsMessage {
  def apply(buffer: FriendlyByteBuf): SyncSkillsMessage = {
    val playerId = buffer.readUUID()
    val force = buffer.readBoolean()
    val size = buffer.readInt()

    PlayerSkillsLogger.SKILLS.debug(s"Received skill sync of $size skills for $playerId")

    val skills = (0 to size)
      .map(_ => buffer.readInt().pipe(buffer.readUtf))
      .flatMap(SkillType().deserialize)
      .toList

    new SyncSkillsMessage(playerId, skills, force)
  }

  def apply(player: MinecraftPlayer[_], skills: List[Skill[_]], force: Boolean): SyncSkillsMessage = {
    new SyncSkillsMessage(player.uuid, skills, force)
  }
}
