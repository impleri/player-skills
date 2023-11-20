package net.impleri.playerskills.client

import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class NetHandler(
  client: Client = Client(),
  messageFactory: ResyncSkillsMessageFactory,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  def resyncPlayer(player: Player[_] = client.getPlayer): Unit = {
    logger.debug(s"Requesting skills resync for ${player.name}")
    player.sendMessage(messageFactory.send(player))
  }
}
