package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class ClientSkillsRegistry(
  eventHandler: EventHandler = EventHandler(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  private var playerSkills: List[Skill[_]] = List.empty

  def get: List[Skill[_]] = playerSkills

  private[client] def update(skills: List[Skill[_]], force: Boolean): Unit = {
    val old = get
    playerSkills = skills
    eventHandler.emitSkillsUpdated(skills, old, force)
  }
}
