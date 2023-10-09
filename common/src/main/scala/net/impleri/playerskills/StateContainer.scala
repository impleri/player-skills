package net.impleri.playerskills

import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

/**
 * Single place for all stateful classes shared between client and server
 */
object StateContainer {
  val SKILLS: SkillRegistry = SkillRegistry()
  val SKILL_TYPES: SkillTypeRegistry = SkillTypeRegistry()
  val EVENT_HANDLERS: EventHandlers = EventHandlers(onSetup = onSetup)

  def init(): Unit = PlayerSkillsLogger.SKILLS.info("PlayerSkills Loaded")

  private def onSetup(): Unit = {
    SKILL_TYPES.resync()
    SKILLS.resync()
  }
}
