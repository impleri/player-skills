package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientStateContainer
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

case class SyncSkillsMessage(
  private val playerId: UUID,
  private val skills: List[Skill[_]],
  private val force: Boolean,
  private val skillTypeOps: SkillTypeOps,
  private val clientStateContainer: Option[ClientStateContainer],
  private val messageType: MessageType,
  private val logger: PlayerSkillsLogger,
) extends BaseS2CMessage {
  override def getType: MessageType = messageType

  override def write(buffer: FriendlyByteBuf): Unit = {
    buffer.writeUUID(playerId)
      .writeBoolean(force)
      .writeInt(skills.size)

    skills.flatMap(skillTypeOps.serialize(_))
      .map(s => (s, s.length))
      .tap(_.map(t => buffer.writeInt(t._2)))
      .tap(_.map(t => buffer.writeUtf(t._1, t._2)))

    logger.debug(s"Sending skill sync of ${skills.size} skills for $playerId")
  }

  override def handle(context: NetworkManager.PacketContext): Unit = {
    clientStateContainer.map(_.getNetHandler).foreach(_.onSyncPlayer(skills, force))
  }
}

case class SyncSkillsMessageFactory(
  skillTypeOps: SkillTypeOps = SkillType(),
  clientStateContainer: Option[ClientStateContainer] = None,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  private var messageType: Option[MessageType] = None

  def setMessageType(newType: MessageType): Unit = {
    messageType = Option(newType)
  }

  private def readSkills(buffer: FriendlyByteBuf, size: Int): Seq[Skill[_]] = {
    List.fill(size)(buffer.readInt())
      .map(buffer.readUtf)
      .flatMap(skillTypeOps.deserialize)
  }

  def receive(
    buffer: FriendlyByteBuf,
  ): SyncSkillsMessage = {
    val playerId = buffer.readUUID()
    val force = buffer.readBoolean()
    val size = buffer.readInt()

    logger.debug(s"Received skill sync of $size skills for $playerId")

    val skills = if (size > 0) readSkills(buffer, size) else Seq.empty

    if (messageType.isEmpty) {
      logger.error(s"Could not handle SYNC_SKILLS without a defined message type")
    }

    SyncSkillsMessage(playerId, skills.toList, force, skillTypeOps, clientStateContainer, messageType.get, logger)
  }

  def send(
    player: Player[_],
    skills: List[Skill[_]],
    force: Boolean,
  ): SyncSkillsMessage = {
    if (messageType.isEmpty) {
      logger.error(s"Could not send SYNC_SKILLS without a defined message type")
    }

    SyncSkillsMessage(player.uuid, skills, force, skillTypeOps, clientStateContainer, messageType.get, logger)
  }
}

object SyncSkillsMessageFactory {
  val NAME: String = "sync_skills"

  def apply(
    skillTypeOps: SkillTypeOps = SkillType(),
    clientStateContainer: Option[ClientStateContainer] = None,
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): SyncSkillsMessageFactory = {
    new SyncSkillsMessageFactory(skillTypeOps, clientStateContainer, logger)
  }
}
