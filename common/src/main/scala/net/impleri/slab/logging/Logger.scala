package net.impleri.slab.logging

import org.apache.logging.log4j.{Logger => Log4Logger}
import org.apache.logging.log4j.LogManager

class Logger(modId: String, private val prefix: String) {
  private def instance: Log4Logger = LogManager.getLogger(modId)

  private var debugEnabled = true

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

  private def addPrefix(message: String): String = s"[$modId][$prefix] $message"

  def error(message: String): Unit = instance.error(addPrefix(message))

  def errorP[T](f: T => String)(value: T): Unit = error(f(value))

  def warn(message: String): Unit = instance.warn(addPrefix(message))

  def warnP[T](f: T => String)(value: T): Unit = warn(f(value))

  def info(message: String): Unit = instance.info(addPrefix(message))

  def infoP[T](f: T => String)(value: T): Unit = info(f(value))

  def debug(message: String): Unit = if (debugEnabled) info(s"[DEBUG]$message") else instance.debug(addPrefix(message))

  def debugP[T](f: T => String)(value: T): Unit = debug(f(value))
}

object Logger {
  final val DEFAULT = new Logger("SLAB", "SLAB")

  def apply(prefix: String, modId: String): Logger = {
    new Logger(
      modId,
      prefix,
    )
  }

  def apply(modId: String)(prefix: String): Logger = {
    new Logger(
      modId,
      prefix,
    )
  }
}
