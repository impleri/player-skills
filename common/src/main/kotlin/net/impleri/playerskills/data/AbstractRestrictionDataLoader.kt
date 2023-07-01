package net.impleri.playerskills.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.tags.TagKey
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull
import net.impleri.playerskills.api.Player as PlayerApi

abstract class AbstractRestrictionDataLoader<Target>(
  val group: String,
  val server: MinecraftServer,
  logger: PlayerSkillsLogger? = null,
) :
  SimpleJsonResourceReloadListener(Gson, group) {
  val logger = logger ?: PlayerSkills.LOGGER

  override fun apply(
    datamap: Map<ResourceLocation, JsonElement>,
    resourceManager: ResourceManager,
    profilerFiller: ProfilerFiller,
  ) {
    datamap.forEach { (name, json) ->
      try {
        parseRestriction(name, json.asJsonObject)?.let {
          logger.info("Registering $group restriction for $name")
          register(name, it)
        }
      } catch (e: Throwable) {
        logger.warn("Could not parse restriction details for $name")
        e.message?.let { logger.error("$it.") }
        e.printStackTrace()
      }
    }
  }

  protected abstract fun parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ): AbstractRestriction<Target>?

  protected abstract fun register(name: ResourceLocation, restriction: AbstractRestriction<Target>)

  protected fun getValue(
    raw: JsonObject,
    key: String,
  ): JsonElement? {
    return try {
      raw.get(key)
    } catch (e: NullPointerException) {
      logger.info("Could not get value for $key")
      e.message?.let { logger.error(it) }
      e.printStackTrace()
      null
    }
  }

  protected fun <T> parseValue(
    raw: JsonObject,
    key: String,
    parser: ((element: JsonElement) -> T),
    defaultValue: T? = null,
  ): T? {
    val rawValue = getValue(raw, key) ?: return null

    return if (rawValue.isJsonNull) defaultValue else parser(rawValue)
  }

  protected fun <T> parseArray(
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

  protected fun <T> parseExclude(raw: JsonElement, callback: (String) -> List<T>): List<T> {
    return parseArray(raw, "exclude", callback)
  }

  protected fun <T> parseInclude(raw: JsonElement, callback: (String) -> List<T>): List<T> {
    return parseArray(raw, "include", callback)
  }

  protected fun parseDimensions(raw: JsonObject): Pair<List<ResourceLocation>, List<ResourceLocation>> {
    return getValue(raw, "dimensions")?.let {
      if (it.isJsonObject) {
        (
          parseInclude(it, this::ifDimension) to parseExclude(
            it,
            this::ifDimension,
          )
          )
      } else {
        null
      }
    } ?: (ArrayList<ResourceLocation>() to ArrayList())
  }

  private fun ifDimension(dimensionName: String): List<ResourceLocation> {
    val list: MutableList<ResourceLocation> = ArrayList()
    val callback: (ResourceLocation) -> Unit = { dim: ResourceLocation -> list.add(dim) }

    val registrationType = RegistrationType(dimensionName, Registry.DIMENSION_REGISTRY)
    registrationType.ifNamespace { ifInDimensionsNamespaced(it, callback) }
    // Note: Dimensions do not have tags, so we cannot use the #tag selector on dimensions
    registrationType.ifName(callback)

    return list
  }

  private fun ifInDimensionsNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    server.levelKeys()
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  protected fun parseBiomes(raw: JsonObject): Pair<List<ResourceLocation>, List<ResourceLocation>> {
    return getValue(raw, "biomes")?.let {
      if (it.isJsonObject) (parseInclude(it, this::ifBiome) to parseExclude(it, this::ifBiome)) else null
    } ?: (ArrayList<ResourceLocation>() to ArrayList())
  }

  private fun ifBiome(dimensionName: String): List<ResourceLocation> {
    val list: MutableList<ResourceLocation> = ArrayList()
    val callback: (ResourceLocation) -> Unit = { dim: ResourceLocation -> list.add(dim) }

    val registrationType = RegistrationType(dimensionName, Registry.BIOME_REGISTRY)
    registrationType.ifNamespace { ifInBiomesNamespaced(it, callback) }
    registrationType.ifTag { ifInBiomesTagged(it, callback) }
    registrationType.ifName(callback)

    return list
  }

  private fun ifInBiomesNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    BuiltinRegistries.BIOME.entrySet()
      .map { it.key }
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  private fun ifInBiomesTagged(tag: TagKey<Biome>, callback: (ResourceLocation) -> Unit) {
    val values = server
      .registryAccess()
      .registry(Registry.BIOME_REGISTRY)
      .map { it.getTag(tag) }
      .getOrNull()

    if (values?.isEmpty != false) {
      logger.warn("No biomes found matching tag ${tag.location}")
      return
    }

    values.get()
      .map { it.unwrapKey() }
      .filter { it.isPresent }
      .map { it.get() }
      .map { it.location() }
      .forEach(callback)
  }

  protected fun parseIf(raw: JsonObject): List<(Player) -> Boolean> {
    val block = getValue(raw, "if") ?: return ArrayList()

    return when {
      block.isJsonNull -> ArrayList()
      block.isJsonArray -> block.asJsonArray.map {
        parseCondition(it.asJsonObject) { player, skill, value ->
          PlayerApi.can(
            player,
            skill,
            value,
          )
        }
      }

      else -> listOf(
        parseCondition(block.asJsonObject) { player, skill, value ->
          PlayerApi.can(
            player,
            skill,
            value,
          )
        },
      )
    }
  }

  protected fun parseUnless(raw: JsonObject): List<(Player) -> Boolean> {
    val block = getValue(raw, "unless") ?: return ArrayList()

    return when {
      block.isJsonNull -> ArrayList()
      block.isJsonArray -> block.asJsonArray.map {
        parseCondition(it.asJsonObject) { player, skill, value ->
          !PlayerApi.can(
            player,
            skill,
            value,
          )
        }
      }

      else -> listOf(
        parseCondition(block.asJsonObject) { player, skill, value ->
          !PlayerApi.can(
            player,
            skill,
            value,
          )
        },
      )
    }
  }

  private fun parseCondition(raw: JsonObject, callback: (Player, String, Any) -> Boolean): (Player) -> Boolean {
    val skillName = parseValue(raw, "skill", { it.asString })
      ?: throw NullPointerException("Expected skill to be defined for the condition")
    val skill =
      Skill.find<Any>(skillName) ?: throw NullPointerException("Expected skill to be defined for the condition")

    val rawValue = parseValue(raw, "value", { it.asString })
    val value = SkillType.find(skill)?.castFromString(rawValue)
      ?: throw NullPointerException("Expected value to be defined for the condition")

    return { player: Player -> callback(player, skillName, value) }
  }

  companion object {
    private val Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
  }
}
