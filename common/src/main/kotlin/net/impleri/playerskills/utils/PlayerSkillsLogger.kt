package net.impleri.playerskills.utils

import net.impleri.playerskills.PlayerSkills
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PlayerSkillsLogger private constructor(modId: String, private val prefix: String) {
  private val instance: Logger
  private var debug = false

  init {
    instance = LogManager.getLogger(modId)
  }

  fun enableDebug() {
    debug = true
  }

  fun disableDebug() {
    debug = false
  }

  fun toggleDebug(): Boolean {
    debug = !debug
    return debug
  }

  private fun addPrefix(message: String): String {
    return "[$prefix] $message"
  }

  fun error(message: String) {
    instance.error(addPrefix(message))
  }

  fun warn(message: String) {
    instance.warn(addPrefix(message))
  }

  fun info(message: String) {
    instance.info(addPrefix(message))
  }

  fun debug(message: String) {
    if (debug) {
      info(message)
      return
    }
    instance.debug(addPrefix(message))
  }

  companion object {
    fun create(modId: String, prefix: String = "MOD"): PlayerSkillsLogger {
      return PlayerSkillsLogger(modId, prefix)
    }

    fun create(prefix: String = "MOD"): PlayerSkillsLogger {
      return PlayerSkillsLogger(PlayerSkills.MOD_ID, prefix)
    }

    enum class TYPES {
      BLOCKS,
      FLUIDS,
      ITEMS,
      MOBS,
      SKILLS,
      SKIPS,
    }

    fun toggleDebug(type: TYPES?): Boolean {
      return when (type) {
        TYPES.BLOCKS -> BLOCKS.toggleDebug()
        TYPES.FLUIDS -> FLUIDS.toggleDebug()
        TYPES.ITEMS -> ITEMS.toggleDebug()
        TYPES.MOBS -> MOBS.toggleDebug()
        TYPES.SKIPS -> SKIPS.toggleDebug()
        else -> SKILLS.toggleDebug()
      }
    }

    internal val SKILLS = create("SKILLS")

    internal val SKIPS = create("REST")

    internal val BLOCKS = create("BLOCKS")

    internal val FLUIDS = create("FLUIDS")

    internal val ITEMS = create("ITEMS")

    internal val MOBS = create("MOBS")
  }
}
