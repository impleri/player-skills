package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.client.Client
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger

case class NetHandler(
  client: Client = Client(),
  clientSkillsRegistry: ClientSkillsRegistry = ClientSkillsRegistry(),
  messageFactory: ResyncSkillsMessageFactory,
  logger: Logger = PlayerSkillsLogger.SKILLS,
) {
  def onSyncPlayer(skills: List[Skill[_]], force: Boolean): Unit = {
    logger.info(
      s"Syncing Client-side skills: ${skills.map(s => s"(${s.name}=${s.value.getOrElse("None")})").mkString(", ")}",
    )

    clientSkillsRegistry.update(skills, force)
  }

  def resyncPlayer(player: Player[_]): Unit = {
    logger.debug(s"Requesting skills resync for ${player.name}")
    messageFactory.send(player).foreach(player.sendMessage)
  }
}
