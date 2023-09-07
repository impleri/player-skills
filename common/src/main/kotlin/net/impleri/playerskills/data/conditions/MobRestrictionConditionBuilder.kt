package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.mobs.MobConditions
import net.impleri.playerskills.restrictions.mobs.MobRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player

class MobRestrictionConditionBuilder(name: ResourceLocation? = null) :
  AbstractRestrictionConditionBuilder<EntityType<*>, MobRestriction>(name),
  MobConditions<Player>,
  RestrictionDataParser {
  override var replacement: EntityType<*>? = null
  override var spawnMode: EntitySpawnMode = EntitySpawnMode.ALLOW_ALWAYS
  override var usable: Boolean? = null
  override var includeSpawners: MutableList<MobSpawnType> = ArrayList()
  override var excludeSpawners: MutableList<MobSpawnType> = ArrayList()

  override fun parseRestriction(
    jsonElement: JsonObject,
  ) {
    parseTarget(jsonElement, "entity")
    parseSpawnMode(jsonElement)
    parseSpawners(jsonElement)
    usable = parseBoolean(jsonElement, "usable", usable)
  }

  override fun toggleEverything() {
    usable = true
  }

  override fun toggleNothing() {
    usable = false
  }

  private fun parseSpawnMode(raw: JsonObject) {
    val spawnable = parseBoolean(raw, "spawnable") ?: false
    val all = parseBoolean(raw, "allPlayers") ?: false
    val always = parseBoolean(raw, "always") ?: false

    spawnMode = when {
      (spawnable && always) -> EntitySpawnMode.ALLOW_ALWAYS
      (spawnable && all) -> EntitySpawnMode.ALLOW_IF_ALL_MATCH
      spawnable -> EntitySpawnMode.ALLOW_IF_ANY_MATCH
      always -> EntitySpawnMode.DENY_ALWAYS
      all -> EntitySpawnMode.DENY_IF_ALL_MATCH
      else -> EntitySpawnMode.DENY_IF_ANY_MATCH
    }
  }

  private fun parseSpawners(raw: JsonObject) {
    getValue(raw, "spawners")?.let {
      if (it.isJsonObject) {
        includeSpawners = parseInclude(it, this::ifSpawner).toMutableList()
        excludeSpawners = parseExclude(it, this::ifSpawner).toMutableList()
      }
    }
  }

  private fun ifSpawner(spawnerName: String): List<MobSpawnType> {
    val spawnType = when {
      (spawnerName == "spawner") -> MobSpawnType.SPAWNER
      (spawnerName == "natural") -> MobSpawnType.NATURAL
      (spawnerName == "chunk") -> MobSpawnType.CHUNK_GENERATION
      (spawnerName == "structure") -> MobSpawnType.STRUCTURE
      (spawnerName == "breeding") -> MobSpawnType.BREEDING
      (spawnerName == "summoned") -> MobSpawnType.MOB_SUMMONED
      (spawnerName == "jockey") -> MobSpawnType.JOCKEY
      (spawnerName == "event") -> MobSpawnType.EVENT
      (spawnerName == "conversion") -> MobSpawnType.CONVERSION
      (spawnerName == "reinforcement") -> MobSpawnType.REINFORCEMENT
      (spawnerName == "triggered") -> MobSpawnType.TRIGGERED
      (spawnerName == "bucket") -> MobSpawnType.BUCKET
      (spawnerName == "egg") -> MobSpawnType.SPAWN_EGG
      (spawnerName == "command") -> MobSpawnType.COMMAND
      (spawnerName == "dispenser") -> MobSpawnType.DISPENSER
      (spawnerName == "patrol") -> MobSpawnType.PATROL
      else -> throw RuntimeException("Unknown spawn type $spawnerName used")
    }

    return listOf(spawnType)
  }
}
