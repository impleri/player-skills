package net.impleri.slab.logging

import org.apache.logging.log4j.{Logger => Log4Logger}
import org.apache.logging.log4j.LogManager

sealed trait LogLevel

object LogLevel {
  final case object TRACE extends LogLevel
  final case object DEBUG extends LogLevel
  final case object INFO extends LogLevel
  final case object WARN extends LogLevel
  final case object ERROR extends LogLevel
}

class Logger(modId: String, private val prefix: String) {
  private def instance: Log4Logger = LogManager.getLogger(modId)

  private var minLogLevel: LogLevel = LogLevel.INFO

  def enableDebug(verbose: Boolean = false): Unit = {
    minLogLevel = if (verbose) LogLevel.TRACE else LogLevel.DEBUG
  }

  def disableDebug(): Unit = {
    minLogLevel = LogLevel.INFO
  }

  private def includesLogLevel(target: LogLevel): Boolean = Logger.includesLogLevel(minLogLevel, target)

  private def debugEnabled: Boolean = includesLogLevel(LogLevel.DEBUG)

  private def traceEnabled: Boolean = includesLogLevel(LogLevel.TRACE)

  def toggleDebug(verbose: Boolean = false): Boolean = {
    if (debugEnabled) disableDebug() else enableDebug(verbose)

    debugEnabled
  }

  private def addPrefix(message: String): String = s"[$prefix] $message"

  def error(message: String): Unit = instance.error(addPrefix(message))

  def error(error: Throwable, message: String = ""): Unit =
    instance.error(addPrefix(message), error)

  def errorP[T](f: T => String)(value: T): Unit = error(f(value))

  def warn(message: String): Unit = instance.warn(addPrefix(message))

  def warn(error: Throwable, message: String = ""): Unit =
    instance.warn(addPrefix(message), error)

  def warnP[T](f: T => String)(value: T): Unit = warn(f(value))

  def info(message: String): Unit = instance.info(addPrefix(message))

  def infoP[T](f: T => String)(value: T): Unit = info(f(value))

  def debug(message: String): Unit = if (debugEnabled) info(s"[DEBUG] $message")
  else instance.debug(addPrefix(message))

  def debugP[T](f: T => String)(value: T): Unit = debug(f(value))

  def trace(message: String): Unit = if (traceEnabled) info(s"[TRACE] $message")
  else instance.debug(addPrefix(message))

  def traceP[T](f: T => String)(value: T): Unit = debug(f(value))
}

object Logger {
  final val DEFAULT = new Logger("SLAB", "SLAB")

  private def LOG_LEVELS: List[LogLevel] = List(
    LogLevel.TRACE,
    LogLevel.DEBUG,
    LogLevel.INFO,
    LogLevel.WARN,
    LogLevel.ERROR,
  )

  private def includesLogLevel(current: LogLevel, target: LogLevel): Boolean = {
    LOG_LEVELS.indexOf(target) >= LOG_LEVELS.indexOf(current)
  }

  def apply(prefix: String, modId: String): Logger = {
    new Logger(
      modId,
      prefix,
    )
  }

  def forMod(modId: String)(prefix: String): Logger = {
    new Logger(
      modId,
      prefix,
    )
  }
}
