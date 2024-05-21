package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class NetHandler(
  client: Client = Client(),
  clientSkillsRegistry: ClientSkillsRegistry = ClientSkillsRegistry(),
  messageFactory: ResyncSkillsMessageFactory,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  def onSyncPlayer(skills: List[Skill[_]], force: Boolean): Unit = {
    logger.info(
      s"Syncing Client-side skills: ${skills.map(s => s"(${s.name}=${s.value.getOrElse("None")})").mkString(", ")}",
    )

    clientSkillsRegistry.update(skills, force)
  }

  def resyncPlayer(player: Player[_]): Unit = {
    logger.debug(s"Requesting skills resync for ${player.name}")
    player.sendMessage(messageFactory.send(player))
  }
}
