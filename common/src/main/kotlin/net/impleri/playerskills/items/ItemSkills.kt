package net.impleri.playerskills.items

import net.impleri.playerskills.utils.PlayerSkillsLogger

object ItemSkills {
  val LOGGER: PlayerSkillsLogger = PlayerSkillsLogger.create("ITEMS")

  fun toggleDebug(): Boolean {
    return LOGGER.toggleDebug()
  }
}
