package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientSkillsRegistry
import net.impleri.playerskills.facades.architectury.Network
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
  private val clientSkillsRegistry: ClientSkillsRegistry,
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
    clientSkillsRegistry.syncFromServer(skills, force)
  }
}

case class SyncSkillsMessageFactory(
  skillTypeOps: SkillTypeOps = SkillType(),
  clientSkillsRegistry: ClientSkillsRegistry = ClientSkillsRegistry(),
  network: Network = Network(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  private var messageType: Option[MessageType] = None

  def setMessageType(newType: MessageType): Unit = {
    messageType = Option(newType)
  }

  def receive(
    buffer: FriendlyByteBuf,
  ): SyncSkillsMessage = {
    val playerId = buffer.readUUID()
    val force = buffer.readBoolean()
    val size = buffer.readInt()

    logger.debug(s"Received skill sync of $size skills for $playerId")

    val skills = (0 to size)
      .map(_ => buffer.readInt().pipe(buffer.readUtf))
      .flatMap(skillTypeOps.deserialize)
      .toList

    if (messageType.isEmpty) {
      logger.error(s"Could not handle SYNC_SKILLS without a defined message type")
    }

    SyncSkillsMessage(playerId, skills, force, skillTypeOps, clientSkillsRegistry, messageType.get, logger)
  }

  def send(
    player: Player[_],
    skills: List[Skill[_]],
    force: Boolean,
  ): SyncSkillsMessage = {
    if (messageType.isEmpty) {
      logger.error(s"Could not send SYNC_SKILLS without a defined message type")
    }

    SyncSkillsMessage(player.uuid, skills, force, skillTypeOps, clientSkillsRegistry, messageType.get, logger)
  }
}

object SyncSkillsMessageFactory {
  val NAME: String = "sync_skills"

  def apply(
    skillTypeOps: SkillTypeOps = SkillType(),
    clientSkillsRegistry: ClientSkillsRegistry = ClientSkillsRegistry(),
    network: Network = Network(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): SyncSkillsMessageFactory = {
    new SyncSkillsMessageFactory(skillTypeOps, clientSkillsRegistry, network, logger)
  }
}
