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

import scala.util.chaining.scalaUtilChainingOps

case class SyncSkillsMessage(
  private val skills: List[Skill[_]],
  private val force: Boolean,
  private val skillTypeOps: SkillTypeOps,
  private val clientStateContainer: Option[ClientStateContainer],
  override val messageType: MessageType,
  private val logger: Logger,
) extends ClientboundMessage {
  override protected[network] def onReceive(player: Option[Player]): Unit =
    for {
      clientState <- clientStateContainer
    } yield clientState.getNetHandler.onSyncPlayer(skills, force)

  override protected[network] def onSend(writer: FriendlyBuffer): Unit = {
    writer
      .writeBoolean(force)

    skills
      .flatMap(skillTypeOps.serialize(_))
      .tap(logger.debugP(s => s"Sending skill sync of ${s.size} skills: $s"))
      .pipe(writer.writeStrings)
  }
}

case class SyncSkillsMessageFactory(
  skillTypeOps: SkillTypeOps = SkillType(),
  clientStateContainer: Option[ClientStateContainer] = None,
  logger: Logger = PlayerSkillsLogger.NETWORK,
) extends MessageFactory[SyncSkillsMessage] {
  final val name: String = "sync_skills"

  override def parse(buffer: FriendlyBuffer, messageType: MessageType): Option[SyncSkillsMessage] =
    for {
      force <- buffer.readBoolean().orElse(Option(false))
      _ = logger.debug(s"Received skill sync with force $force")
      skills = buffer.readStrings().flatMap(skillTypeOps.deserialize).toList
    } yield {
      logger.info(s"Received skill sync of ${skills.size} skills")

      SyncSkillsMessage(
        skills,
        force,
        skillTypeOps,
        clientStateContainer,
        messageType,
        logger,
      )
    }

  def send(
    skills: List[Skill[_]],
    force: Boolean,
  ): Option[SyncSkillsMessage] =
    createForSend(
      SyncSkillsMessage(
        skills,
        force,
        skillTypeOps,
        clientStateContainer,
        _,
        logger,
      ),
    )
}
