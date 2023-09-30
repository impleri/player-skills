package net.impleri.playerskills.utils

import net.impleri.playerskills.PlayerSkills
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

sealed trait LoggerType
object LoggerType {
  case object BLOCKS extends LoggerType
  case object FLUIDS extends LoggerType
  case object ITEMS extends LoggerType
  case object MOBS extends LoggerType
  case object SKILLS extends LoggerType
  case object SKIPS extends LoggerType
}

class PlayerSkillsLogger(modId: String, private val prefix: String) {
  private def instance: Logger = LogManager.getLogger(modId)
  private var debugEnabled = false

  def enableDebug(): Unit = {
    debugEnabled = true
  }

  def disableDebug(): Unit = {
    debugEnabled = false
  }

  def toggleDebug(): Boolean = {
    debugEnabled = !debugEnabled
    debugEnabled
  }

  private def addPrefix(message: String): String = s"[SKILLS][$prefix] $message"

  def error(message: String): Unit = instance.error(addPrefix(message))

  def warn(message: String): Unit = instance.warn(addPrefix(message))

  def info(message: String): Unit = instance.info(addPrefix(message))

  def debug(message: String): Unit = if (debugEnabled) info(s"[DEBUG]$message") else instance.debug(addPrefix(message))
}

object PlayerSkillsLogger {
  private def apply(prefix: String, modId: String = PlayerSkills.MOD_ID): PlayerSkillsLogger = new PlayerSkillsLogger(modId, prefix)

  val SKILLS: PlayerSkillsLogger = PlayerSkillsLogger("CORE")
  val SKIPS: PlayerSkillsLogger  = PlayerSkillsLogger("REST")
  val BLOCKS: PlayerSkillsLogger = PlayerSkillsLogger("BLOCKS")
  val FLUIDS: PlayerSkillsLogger = PlayerSkillsLogger("FLUIDS")
  val ITEMS: PlayerSkillsLogger  = PlayerSkillsLogger("ITEMS")
  val MOBS: PlayerSkillsLogger   = PlayerSkillsLogger("MOBS")

  def toggleDebug(logType: Option[LoggerType] = None): Boolean =
    logType match {
      case Some(LoggerType.BLOCKS) => BLOCKS.toggleDebug()
      case Some(LoggerType.FLUIDS) => FLUIDS.toggleDebug()
      case Some(LoggerType.ITEMS)  => ITEMS.toggleDebug()
      case Some(LoggerType.MOBS)   => MOBS.toggleDebug()
      case Some(LoggerType.SKIPS)  => SKIPS.toggleDebug()
      case _                       => SKILLS.toggleDebug()
    }
}
