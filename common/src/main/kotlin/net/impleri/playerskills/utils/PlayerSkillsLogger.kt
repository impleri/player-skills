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
  }
}
