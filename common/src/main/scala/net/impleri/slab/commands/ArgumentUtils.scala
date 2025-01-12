package net.impleri.slab.commands

import net.impleri.slab.logging.Logger

import scala.util.{Failure, Success, Try}

trait  ArgumentUtils {
  def logger: Option[Logger] = None

  private def logIssue(message: String): Unit = logger.foreach(_.warn(message))

  def wrapParser[U, T](argumentType: String, name: String, convert: U => T)(f: => U): Option[T] =
    Try(f) match {
      case Success(rawValue) => Option(rawValue)
        .map(convert)
        .orElse {
          logIssue(s"Could not find a $argumentType argument named $name")
          None
        }
      case Failure(exception) =>
        logIssue(s"Could not parse $argumentType argument $name: $exception")
        None
    }
}
