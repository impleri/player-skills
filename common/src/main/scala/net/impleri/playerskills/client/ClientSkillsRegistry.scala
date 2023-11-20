package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class ClientSkillsRegistry(
  eventHandler: EventHandler = EventHandler(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  private var playerSkills: List[Skill[_]] = List.empty

  def get: List[Skill[_]] = playerSkills

  def syncFromServer(skills: List[Skill[_]], force: Boolean): Unit = {
    val old = get

    logger
      .info(s"Syncing Client-side skills: ${
        skills
          .map(s => s"(${s.name}=${s.value.getOrElse("None")})")
          .mkString(", ")
      }",
      )

    playerSkills = skills

    eventHandler.emitSkillsUpdated(skills, old, force)
  }
}
