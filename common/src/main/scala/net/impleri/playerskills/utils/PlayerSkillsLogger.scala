package net.impleri.playerskills.utils

import net.impleri.playerskills.PlayerSkills
import net.impleri.slab.logging.Logger

sealed trait LoggerType

object LoggerType {
  final case object BLOCKS extends LoggerType

  final case object FLUIDS extends LoggerType

  final case object FTB extends LoggerType

  final case object ITEMS extends LoggerType

  final case object MOBS extends LoggerType

  final case object NETWORK extends LoggerType

  final case object RESTRICTIONS extends LoggerType

  final case object SKILLS extends LoggerType

  final case object STORAGE extends LoggerType
}

object PlayerSkillsLogger {
  private val factory: String => Logger = Logger.forMod(PlayerSkills.MOD_ID)

  val BLOCKS: Logger = factory("BLOCKS")
  val EVENTS: Logger = factory("EVENTS")
  val FLUIDS: Logger = factory("FLUIDS")
  val FTB: Logger = factory("FTB")
  val ITEMS: Logger = factory("ITEMS")
  val MOBS: Logger = factory("MOBS")
  val NETWORK: Logger = factory("NET")
  val RESTRICTIONS: Logger = factory("CAN")
  val SKILLS: Logger = factory("CORE")
  val STORAGE: Logger = factory("FILE")

  def toggleDebug(logType: Option[LoggerType] = None): Boolean =
    logType match {
      case Some(LoggerType.BLOCKS)       => BLOCKS.toggleDebug()
      case Some(LoggerType.FLUIDS)       => FLUIDS.toggleDebug()
      case Some(LoggerType.FTB)          => FTB.toggleDebug()
      case Some(LoggerType.ITEMS)        => ITEMS.toggleDebug()
      case Some(LoggerType.MOBS)         => MOBS.toggleDebug()
      case Some(LoggerType.NETWORK)      => NETWORK.toggleDebug()
      case Some(LoggerType.RESTRICTIONS) => RESTRICTIONS.toggleDebug()
      case Some(LoggerType.STORAGE)      => STORAGE.toggleDebug()
      case _                             => SKILLS.toggleDebug()
    }
}
