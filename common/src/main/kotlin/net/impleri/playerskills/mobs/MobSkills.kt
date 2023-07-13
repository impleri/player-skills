package net.impleri.playerskills.mobs

import net.impleri.playerskills.utils.PlayerSkillsLogger

object MobSkills {
  val LOGGER: PlayerSkillsLogger = PlayerSkillsLogger.create("MOBS")

  fun toggleDebug(): Boolean {
    return LOGGER.toggleDebug()
  }
}
