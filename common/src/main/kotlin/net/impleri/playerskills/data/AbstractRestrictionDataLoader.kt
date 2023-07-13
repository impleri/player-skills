package net.impleri.playerskills.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

abstract class AbstractRestrictionDataLoader<Target, Restriction : AbstractRestriction<Target>>(
  group: String,
) :
  SimpleJsonResourceReloadListener(Gson, group),
  RestrictionDataParser {
  override fun apply(
    datamap: Map<ResourceLocation, JsonElement>,
    resourceManager: ResourceManager,
    profilerFiller: ProfilerFiller,
  ) {
    datamap.forEach { (name, json) -> parseRestriction(name, json.asJsonObject) }
  }

  protected abstract fun parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  )

  companion object {
    private val Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
  }
}
