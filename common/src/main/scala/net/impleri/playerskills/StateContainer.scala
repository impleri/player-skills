package net.impleri.playerskills

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.bindings.LifecycleEvents
import net.impleri.playerskills.facades.architectury.Network
import net.impleri.playerskills.facades.architectury.Registrar
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

/**
 * Single place for all stateful classes shared between client and server
 */
case class StateContainer(
  skillRegistrar: Registrar[Skill[_]] = Registrar(None),
  skillTypeRegistrar: Registrar[SkillType[_]] = Registrar(None),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  val SKILL_TYPES: SkillTypeRegistry = SkillTypeRegistry(skillTypeRegistrar)
  val SKILLS: SkillRegistry = SkillRegistry(gameRegistrar = skillRegistrar)

  lazy val SKILL_TYPE_OPS: SkillTypeOps = SkillType(SKILL_TYPES)

  lazy val SKILL_OPS: SkillOps = Skill(getSkillTypeOps, SKILLS)
  
  val NETWORK: Network = Network()
  private val LIFECYCLE = LifecycleEvents(onSetup)

  logger.info("PlayerSkills Loaded")
  LIFECYCLE.registerEvents()

  private def onSetup(): Unit = {
    SKILL_TYPES.resync()
    SKILLS.resync()
  }

  def getSkillTypeOps: SkillTypeOps = SKILL_TYPE_OPS

  def getSkillOps: SkillOps = SKILL_OPS
}
