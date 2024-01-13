package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

trait BiomeFacetParser extends JsonDataParser {
  protected def parseBiomes(
    raw: JsonObject,
    onInclude: JsonElement => Unit,
    onExclude: JsonElement => Unit,
  ): Unit = {
    parseFacet(raw, "biomes", onInclude, onExclude)
  }
}
