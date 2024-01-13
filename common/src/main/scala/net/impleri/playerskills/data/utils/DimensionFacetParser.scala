package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

trait DimensionFacetParser extends JsonDataParser {
  protected def parseDimensions(
    raw: JsonObject,
    onInclude: JsonElement => Unit,
    onExclude: JsonElement => Unit,
  ): Unit = {
    parseFacet(raw, "dimensions", onInclude, onExclude)
  }
}
