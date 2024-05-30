package net.impleri.playerskills.network

import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.FriendlyBuffer
import net.impleri.slab.network.MessageFactory
import net.impleri.slab.network.MessageFactory.ReceiveFn
import net.impleri.slab.network.MessageType
import net.impleri.slab.network.ServerboundMessage

import java.util.UUID

case class ResyncSkillsMessage(
  private val playerId: UUID,
  private val serverStateContainer: Option[ServerStateContainer],
  override val messageType: MessageType,
)
  extends ServerboundMessage {
  def write(buffer: FriendlyBuffer): Unit = buffer.writeUUID(playerId)

  override def onReceive: () => Unit = {
    () => {
      val player = serverStateContainer.flatMap(_.SERVER).flatMap(_.getPlayer(playerId))
      val netHandler = serverStateContainer.map(_.getNetHandler)

      (player, netHandler) match {
        case (Some(player: Player[_]), Some(netHandler: NetHandler)) => netHandler.syncPlayer(player)
        case _ =>
      }
    }
  }
}

case class ResyncSkillsMessageFactory(
  serverStateContainer: Option[ServerStateContainer] = None,
  logger: Logger = PlayerSkillsLogger.SKILLS,
) extends MessageFactory[ResyncSkillsMessage] {
  final val name: String = "resync_skills"

  def send(
    player: Player[_],
  ): Option[ResyncSkillsMessage] = {
    createForSend(ResyncSkillsMessage(player.uuid, serverStateContainer, _))
  }

  override protected def onReceive: ReceiveFn[ResyncSkillsMessage] = {
    (buffer, messageType) => {
      val playerId = buffer.readUUID()

      ResyncSkillsMessage(playerId.get, serverStateContainer, messageType)
    }
  }
}
