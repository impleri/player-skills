package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID

case class ResyncSkillsMessage(
  private val playerId: UUID,
  private val serverStateContainer: ServerStateContainer,
  private val messageType: MessageType,
)
  extends BaseC2SMessage {
  override def getType: MessageType = messageType

  override def write(buffer: FriendlyByteBuf): Unit = buffer.writeUUID(playerId)

  // Server-side
  override def handle(context: NetworkManager.PacketContext): Unit = {
    serverStateContainer.SERVER.foreach { server =>
      val netHandler = serverStateContainer.getNetHandler
      server.getPlayer(playerId).foreach(netHandler.syncPlayer(_))
    }
  }
}

case class ResyncSkillsMessageFactory(
  serverStateContainer: ServerStateContainer,
  logger: PlayerSkillsLogger,
) {
  private var messageType: Option[MessageType] = None

  def setMessageType(newType: MessageType): Unit = {
    messageType = Option(newType)
  }

  // Server-side
  def receive(
    buffer: FriendlyByteBuf,

  ): ResyncSkillsMessage = {
    val playerId = buffer.readUUID()

    if (messageType.isEmpty) {
      logger.error(s"Could not handle RESYNC_SKILLS without a defined message type")
    }

    ResyncSkillsMessage(playerId, serverStateContainer, messageType.get)
  }

  // Client-side
  def send(
    player: Player[_],
  ): ResyncSkillsMessage = {
    if (messageType.isEmpty) {
      logger.error(s"Could not send RESYNC_SKILLS without a defined message type")
    }

    ResyncSkillsMessage(player.uuid, serverStateContainer, messageType.get)
  }
}

object ResyncSkillsMessageFactory {
  val NAME: String = "resync_skills"

  def apply(
    serverStateContainer: ServerStateContainer = ServerStateContainer(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): ResyncSkillsMessageFactory = {
    new ResyncSkillsMessageFactory(serverStateContainer, logger)
  }
}
