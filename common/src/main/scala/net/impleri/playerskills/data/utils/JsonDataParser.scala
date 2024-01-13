package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

trait JsonDataParser extends JsonCollectionParser {
  protected[utils] def parseOptions[T](
    raw: JsonObject,
    parser: JsonElement => Option[T],
  ): List[T] = {
    parseArray(
      raw,
      "options",
      parser,
    )
  }

  protected[utils] def parseExclude[T](raw: JsonElement, callback: JsonElement => Option[T]): List[T] = {
    parseArray(raw, "exclude",
      callback,
    )
  }

  protected def parseExcludeAction(raw: JsonElement, callback: JsonElement => Unit): Unit = {
    parseArrayEach(raw, "exclude",
      callback,
    )
  }

  protected[utils] def parseInclude[T](raw: JsonElement, callback: JsonElement => Option[T]): List[T] = {
    parseArray(raw, "include",
      callback,
    )
  }

  protected def parseIncludeAction(raw: JsonElement, callback: JsonElement => Unit): Unit = {
    parseArrayEach(raw, "include",
      callback,
    )
  }

  protected[utils] def parseFacet(
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
