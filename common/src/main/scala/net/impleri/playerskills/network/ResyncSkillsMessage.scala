package net.impleri.playerskills.network

import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.FriendlyBuffer
import net.impleri.slab.network.MessageFactory
import net.impleri.slab.network.MessageType
import net.impleri.slab.network.ServerboundMessage

case class ResyncSkillsMessage(
  private val serverStateContainer: Option[ServerStateContainer],
  override val messageType: MessageType,
  logger: Logger,
) extends ServerboundMessage {
  override protected[network] def onReceive(playerOpt: Option[Player]): Unit =
      for {
        serverState <- serverStateContainer
        player <- playerOpt
      } yield serverState.getNetHandler.syncPlayer(player)

  def onSend(buffer: FriendlyBuffer): Unit = {
    // Write something to the buffer so that it's non-empty
    buffer.writeBoolean(true)
    logger.debug(s"Sending skill resync request")
  }
}

case class ResyncSkillsMessageFactory(
  serverStateContainer: Option[ServerStateContainer] = None,
  logger: Logger = PlayerSkillsLogger.NETWORK,
) extends MessageFactory[ResyncSkillsMessage] {
  final val name: String = "resync_skills"

  override protected def parse(buffer: FriendlyBuffer, messageType: MessageType): Option[ResyncSkillsMessage] = {
    logger.info(s"Received skill resync request")

    Option(ResyncSkillsMessage(serverStateContainer, messageType, logger))
  }

  def send(): Option[ResyncSkillsMessage] =
    createForSend(ResyncSkillsMessage(serverStateContainer, _, logger))
}
