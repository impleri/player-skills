package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait JsonCollectionParser extends JsonValueParser {
  protected[utils] def parseArray[T](
    raw: JsonElement,
    key: String,
    parser: JsonElement => Option[T],
  ): List[T] = {
    parseValue(
      raw.getAsJsonObject,
      key,
      _.getAsJsonArray
        .asScala
        .toList
        .flatMap(parser)
        .pipe(Option(_)),
    ).toList.flatten
  }

  protected[utils] def parseArrayEach(
    raw: JsonElement,
    key: String,
    callback: JsonElement => Unit,
  ): Unit = {
    parseValue(
      raw.getAsJsonObject,
      key,
      _.getAsJsonArray
        .asScala
        .foreach(callback)
        .pipe(_ => None),
    )
  }

  protected def parseObjectOrArray(raw: JsonObject, key: String): List[JsonElement] = {
    getElement(raw, key) match {
      case Some(list) if list.isJsonArray => list.getAsJsonArray.asScala.toList
      case Some(obj) if obj.isJsonObject => List(obj.getAsJsonObject)
      case _ => List.empty
    }
  }
}
