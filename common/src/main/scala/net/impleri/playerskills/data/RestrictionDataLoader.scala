package net.impleri.playerskills.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.BiomeFacetParser
import net.impleri.playerskills.data.utils.ConditionDataParser
import net.impleri.playerskills.data.utils.DimensionFacetParser
import net.impleri.playerskills.data.utils.JsonDataParser
import net.impleri.slab.resources.JsonResourceReloadListener
import net.impleri.slab.resources.ResourceLocation

abstract class RestrictionDataLoader(group: String)
    extends JsonResourceReloadListener(group)
    with JsonDataParser
    with BiomeFacetParser
    with DimensionFacetParser
    with ConditionDataParser {
  protected def parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ): Unit

  override def parse(
    data: Map[ResourceLocation, JsonElement],
  ): Unit =
    data.foreach(t => parseRestriction(t._1, t._2.getAsJsonObject))
}
