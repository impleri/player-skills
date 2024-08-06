package net.impleri.playerskills.utils

import net.impleri.playerskills.PlayerSkills
import net.impleri.slab.logging.Logger

sealed trait LoggerType

object LoggerType {
  final case object BLOCKS extends LoggerType

  final case object FLUIDS extends LoggerType

  final case object ITEMS extends LoggerType

  final case object MOBS extends LoggerType

  final case object SKILLS extends LoggerType

  final case object SKIPS extends LoggerType
}

object PlayerSkillsLogger {
  private val factory: String => Logger = Logger.forMod(PlayerSkills.MOD_ID)

  val SKILLS: Logger = factory("CORE")
  val SKIPS: Logger = factory("REST")
  val BLOCKS: Logger = factory("BLOCKS")
  val FLUIDS: Logger = factory("FLUIDS")
  val ITEMS: Logger = factory("ITEMS")
  val MOBS: Logger = factory("MOBS")

  def toggleDebug(logType: Option[LoggerType] = None): Boolean = {
    logType match {
      case Some(LoggerType.BLOCKS) => BLOCKS.toggleDebug()
      case Some(LoggerType.FLUIDS) => FLUIDS.toggleDebug()
      case Some(LoggerType.ITEMS)  => ITEMS.toggleDebug()
      case Some(LoggerType.MOBS)   => MOBS.toggleDebug()
      case Some(LoggerType.SKIPS)  => SKIPS.toggleDebug()
      case _                       => SKILLS.toggleDebug()
    }
  }
}
