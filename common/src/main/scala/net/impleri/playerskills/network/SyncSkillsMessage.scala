package net.impleri.playerskills.network

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.ClientboundMessage
import net.impleri.slab.network.FriendlyBuffer
import net.impleri.slab.network.MessageFactory
import net.impleri.slab.network.MessageType

import java.util.UUID

case class SyncSkillsMessage(
  private val playerId: UUID,
  private val skills: List[Skill[_]],
  private val force: Boolean,
  private val skillTypeOps: SkillTypeOps,
  private val clientStateContainer: Option[ClientStateContainer],
  override val messageType: MessageType,
  private val logger: Logger,
) extends ClientboundMessage {
  override def onReceive: () => Unit = {
    () =>
    clientStateContainer
      .map(_.getNetHandler)
      .foreach(_.onSyncPlayer(skills, force))
  }

  override def write(writer: FriendlyBuffer): Unit = {
    writer.writeUUID(playerId)
      .writeBoolean(force)
      .writeInt(skills.size)

    skills.flatMap(skillTypeOps.serialize(_))
      .map(writer.writeString)

    logger.debug(s"Sending skill sync of ${skills.size} skills for $playerId")
  }
}

case class SyncSkillsMessageFactory(
  skillTypeOps: SkillTypeOps = SkillType(),
  clientStateContainer: Option[ClientStateContainer] = None,
  logger: Logger = PlayerSkillsLogger.SKILLS,
) extends MessageFactory[SyncSkillsMessage] {
  final val name: String = "sync_skills"

  override val onReceive: MessageFactory.ReceiveFn[SyncSkillsMessage] = (buffer, messageType) => {
    val playerId = buffer.readUUID()
    val force = buffer.readBoolean()
    val skills = buffer.readStrings().flatMap(skillTypeOps.deserialize)

    logger.debug(s"Received skill sync of ${skills.size} skills for $playerId")

    SyncSkillsMessage(
      playerId.get,
      skills.toList,
      force.getOrElse(false),
      skillTypeOps,
      clientStateContainer,
      messageType,
      logger,
    )
  }

  def send(
    player: Player[_],
    skills: List[Skill[_]],
    force: Boolean,
  ): Option[SyncSkillsMessage] = {
    createForSend(SyncSkillsMessage(player.uuid, skills, force, skillTypeOps, clientStateContainer, _, logger))
  }
}
