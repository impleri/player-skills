package net.impleri.playerskills

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.facades.ArchitecturyRegistrar
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

/**
 * Single place for all stateful classes shared between client and server
 */
case class StateContainer(
  skillRegistrar: ArchitecturyRegistrar[Skill[_]] = ArchitecturyRegistrar(None),
  skillTypeRegistrar: ArchitecturyRegistrar[SkillType[_]] = ArchitecturyRegistrar(None),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  logger.info("PlayerSkills Loaded")

  val SKILLS: SkillRegistry = SkillRegistry(gameRegistrar = skillRegistrar)
  val SKILL_TYPES: SkillTypeRegistry = SkillTypeRegistry(skillTypeRegistrar)
  val EVENT_HANDLERS: EventHandlers = EventHandlers(onSetup = onSetup)

  private def onSetup(): Unit = {
    SKILL_TYPES.resync()
    SKILLS.resync()
  }

  def getSkillTypeOps: SkillTypeOps = SkillType(SKILL_TYPES)

  def getSkillOps: SkillOps = Skill(getSkillTypeOps, SKILLS)
}
