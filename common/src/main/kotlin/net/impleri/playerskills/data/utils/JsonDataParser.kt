package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
interface JsonDataParser {
  fun getValue(
    raw: JsonObject,
    key: String,
  ): JsonElement? {
    return raw.get(key)
  }

  fun <T> parseValue(
    raw: JsonObject,
    key: String,
    parser: ((element: JsonElement) -> T),
    defaultValue: T? = null,
  ): T? {
    val rawValue = getValue(raw, key) ?: return null

    return if (rawValue.isJsonNull) defaultValue else parser(rawValue)
  }

  fun parseBoolean(
    raw: JsonObject,
    key: String,
    defaultValue: Boolean? = null,
  ): Boolean? {
    return parseValue(raw, key, { it.asBoolean }, defaultValue)
  }

  fun parseInt(
    raw: JsonObject,
    key: String,
    defaultValue: Int? = null,
  ): Int? {
    return parseValue(raw, key, { it.asInt }, defaultValue)
  }

  fun parseString(
    raw: JsonObject,
    key: String,
    defaultValue: String? = null,
  ): String? {
    return parseValue(raw, key, { it.asString }, defaultValue)
  }

  fun <T> parseArray(
    raw: JsonElement,
    key: String,
    callback: (String) -> List<T>,
  ): List<T> {
    return parseValue(
      raw.asJsonObject,
      key,
      {
        it.asJsonArray.flatMap { rawValue -> callback(rawValue.asString) }
      },
    ) ?: ArrayList()
  }

  fun parseArrayEach(
    raw: JsonElement,
    key: String,
    callback: (String) -> Unit,
  ) {
    parseValue(
      raw.asJsonObject,
      key,
      { it.asJsonArray.forEach { item -> callback(item.asString) } },
    )
  }

  fun parseObjectOrArray(raw: JsonObject, key: String): List<JsonElement> {
    val block = getValue(raw, key) ?: return ArrayList()

    return when {
      block.isJsonArray -> block.asJsonArray.toList()
      block.isJsonObject -> listOf(block.asJsonObject)
      else -> ArrayList()
    }
  }

  fun <T> parseExclude(raw: JsonElement, callback: (String) -> List<T>): List<T> {
    return parseArray(raw, "exclude", callback)
  }

  fun parseExcludeAction(raw: JsonElement, callback: (String) -> Unit) {
    parseArrayEach(raw, "exclude", callback)
  }

  fun <T> parseInclude(raw: JsonElement, callback: (String) -> List<T>): List<T> {
    return parseArray(raw, "include", callback)
  }

  fun parseIncludeAction(raw: JsonElement, callback: (String) -> Unit) {
    parseArrayEach(raw, "include", callback)
  }

  fun parseFacet(
    raw: JsonObject,
    key: String,
    onInclude: (value: String) -> Unit,
    onExclude: (value: String) -> Unit,
  ) {
    getValue(raw, key)?.let {
      when {
        it.isJsonObject -> {
          parseIncludeAction(it, onInclude)
          parseExcludeAction(it, onExclude)
        }

        it.isJsonArray -> it.asJsonArray.forEach { value -> onInclude(value.asString) }
        else -> null
      }
    }
  }
}
