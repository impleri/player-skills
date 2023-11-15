package net.impleri.playerskills.client

import net.impleri.playerskills.facades.MinecraftClient
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.network.ResyncSkillsMessage
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class NetHandler(
  client: MinecraftClient = MinecraftClient(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  def resyncPlayer(player: MinecraftPlayer[_] = client.getPlayer): Unit = {
    logger.debug(s"Requesting skills resync for ${player.name}")
    player.sendMessage(ResyncSkillsMessage(player))
  }
}
