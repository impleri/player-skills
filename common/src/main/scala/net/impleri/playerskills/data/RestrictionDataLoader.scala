package net.impleri.playerskills.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.BiomeFacetParser
import net.impleri.playerskills.data.utils.ConditionDataParser
import net.impleri.playerskills.data.utils.DimensionFacetParser
import net.impleri.playerskills.data.utils.JsonDataParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

import scala.jdk.CollectionConverters._

abstract class RestrictionDataLoader(group: String)
  extends SimpleJsonResourceReloadListener(RestrictionDataLoader.GsonService, group)
    with JsonDataParser
    with BiomeFacetParser
    with DimensionFacetParser
    with ConditionDataParser {
  protected def parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ): Unit

  override def apply(
    data: java.util.Map[ResourceLocation, JsonElement],
    resourceManager: ResourceManager,
    profilerFiller: ProfilerFiller,
  ): Unit = {
    data.asScala.foreach(t => parseRestriction(t._1, t._2.getAsJsonObject))
  }
}

object RestrictionDataLoader {
  private[data] val GsonService: Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
}
