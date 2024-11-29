package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.client.{Client, Player}
import net.impleri.slab.logging.Logger

case class NetHandler(
  clientSkillsRegistry: ClientSkillsRegistry = ClientSkillsRegistry(),
  messageFactory: ResyncSkillsMessageFactory,
  logger: Logger = PlayerSkillsLogger.NETWORK,
) {
  def onSyncPlayer(skills: List[Skill[_]], force: Boolean): Unit = {
    logger.info(
      s"Syncing Client-side skills: ${skills.map(s => s"(${s.name}=${s.value.getOrElse("None")})").mkString(", ")}",
    )

    clientSkillsRegistry.update(skills, force)
  }

  def resyncPlayer(player: Player): Unit = {
    logger.debug(s"Requesting skills resync for ${player.handle}")
    messageFactory.send().foreach(player.sendMessage)
  }
}
