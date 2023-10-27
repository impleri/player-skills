package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.jdk.javaapi.CollectionConverters
import scala.util.Failure
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait JsonDataParser {
  protected def logger: PlayerSkillsLogger

  private def getElement(
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

  private def parseValueHelper[T](parser: JsonElement => T)(element: JsonElement): Option[T] = {
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
    parser: JsonElement => T,
    defaultValue: Option[T] = None,
  ): Option[T] = {
    getElement(raw, key)
      .flatMap(parseValueHelper(parser))
      .orElse(defaultValue)
  }

  protected def parseBoolean(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Boolean] = None,
  ): Option[Boolean] = {
    parseValue(raw, key, _.getAsBoolean, defaultValue)
  }

  protected def parseInt(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Int] = None,
  ): Option[Int] = {
    parseValue(raw, key, _.getAsInt, defaultValue)
  }

  protected def parseDouble(
    raw: JsonObject,
    key: String,
    defaultValue: Option[Double] = None,
  ): Option[Double] = {
    parseValue(raw, key, _.getAsDouble, defaultValue)
  }

  protected def parseString(
    raw: JsonObject,
    key: String,
    defaultValue: Option[String] = None,
  ): Option[String] = {
    parseValue(raw, key, _.getAsString, defaultValue)
  }

  protected def parseArray[T](
    raw: JsonElement,
    key: String,
    parser: JsonElement => T,
  ): List[T] = {
    parseValue(
      raw.getAsJsonObject,
      key,
      _.getAsJsonArray.pipe(a => CollectionConverters.asScala(a)).map(parser).toList,
    ).getOrElse(List.empty)
  }

  protected def parseArrayEach(
    raw: JsonElement,
    key: String,
    callback: JsonElement => Unit,
  ): Unit = {
    parseValue(
      raw.getAsJsonObject,
      key,
      _.getAsJsonArray.pipe(a => CollectionConverters.asScala(a)).foreach(callback),
    )
  }

  protected def parseObjectOrArray(raw: JsonObject, key: String): List[JsonElement] = {
    getElement(raw, key) match {
      case Some(list) if list.isJsonArray => list.getAsJsonArray.pipe(a => CollectionConverters.asScala(a)).toList
      case Some(obj) if obj.isJsonObject => List(obj.getAsJsonObject)
      case _ => List.empty
    }
  }

  protected def parseOptions[T](
    raw: JsonObject,
    parser: JsonElement => T,
  ): List[T] = {
    parseArray(
      raw,
      "options",
      parser,
    )
  }

  protected def parseExclude[T](raw: JsonElement, callback: JsonElement => T): List[T] = {
    parseArray(raw, "exclude",
      callback,
    )
  }

  protected def parseExcludeAction(raw: JsonElement, callback: JsonElement => Unit) = {
    parseArrayEach(raw, "exclude",
      callback,
    )
  }

  protected def parseInclude[T](raw: JsonElement, callback: JsonElement => T): List[T] = {
    parseArray(raw, "include",
      callback,
    )
  }

  protected def parseIncludeAction(raw: JsonElement, callback: JsonElement => Unit) = {
    parseArrayEach(raw, "include",
      callback,
    )
  }

  protected def parseFacet(
    raw: JsonObject,
    key: String,
    onInclude: JsonElement => Unit,
    onExclude: JsonElement => Unit,
  ): Unit = {
    getElement(raw, key) match {
      case Some(obj) if obj.isJsonObject => {
        parseIncludeAction(obj, onInclude)
        parseExcludeAction(obj, onExclude)
      }
      case Some(list) if list.isJsonArray => {
        list.getAsJsonArray
          .pipe(a => CollectionConverters.asScala(a))
          .foreach(onInclude)
      }
      case _ =>
    }
  }
}
