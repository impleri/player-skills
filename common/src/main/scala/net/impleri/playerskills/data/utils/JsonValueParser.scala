package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.util.Failure
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait JsonValueParser {
  protected def logger: PlayerSkillsLogger

  private[utils] def getElement(
    raw: JsonObject,
    key: String,
  ): Option[JsonElement] = {
    Try(raw.get(key)).tap {
      case Failure(e: NullPointerException) =>
      logger.info(s"Could not get value for $key")
      logger.error(e.getMessage)
      case _ => ()
    }.toOption.flatMap(Option(_))
  }

  private[utils] def parseValueHelper[T](parser: JsonElement => T)(element: JsonElement): Option[T] = {
    Try(parser(element)).tap {
      case Failure(e) =>
      logger.info(s"Could not parse value for ${element.getAsString}")
      logger.error(e.getMessage)
      case _ => ()
    }.toOption
  }

  protected def parseValue[T](
    raw: JsonObject,
    key: String,
    parser: JsonElement => Option[T],
    defaultValue: Option[T] = None,
  ): Option[T] = {
    getElement(raw, key)
      .flatMap(parseValueHelper(parser))
      .getOrElse(defaultValue)
  }

  protected def isPrimitiveType(value: JsonElement, f: JsonPrimitive => Boolean): Boolean = {
    value.isJsonPrimitive && f(value.asInstanceOf[JsonPrimitive])
  }

  private def wrapCast[T](raw: JsonElement, f: JsonPrimitive => Boolean, t: JsonElement => T): Option[T] = {
    if (isPrimitiveType(raw, f)) Option(t(raw)) else None
  }

  protected[utils] def castAsBoolean(raw: JsonElement): Option[Boolean] = {
    wrapCast(raw, _.isBoolean, _.getAsBoolean)
  }

  protected[utils] def parseBoolean(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Boolean] = None,
  ): Option[Boolean] = {
    parseValue(raw, key, castAsBoolean, defaultValue)
  }

  protected[utils] def castAsInt(raw: JsonElement): Option[Int] = {
    wrapCast(raw, _.isNumber, _.getAsInt)
  }

  protected[utils] def parseInt(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Int] = None,
  ): Option[Int] = {
    parseValue(raw, key, castAsInt, defaultValue)
  }

  protected[utils] def castAsDouble(raw: JsonElement): Option[Double] = {
    wrapCast(raw, _.isNumber, _.getAsDouble)
  }

  protected[utils] def parseDouble(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Double] = None,
  ): Option[Double] = {
    parseValue(raw, key, castAsDouble, defaultValue)
  }

  protected[utils] def castAsString(raw: JsonElement): Option[String] = {
    wrapCast(raw, _.isString, _.getAsString)
  }

  protected[utils] def parseString(
    raw: JsonObject,
    key: String,
    defaultValue: Option[String] = None,
  ): Option[String] = {
    parseValue(raw, key, castAsString, defaultValue)
  }
}
