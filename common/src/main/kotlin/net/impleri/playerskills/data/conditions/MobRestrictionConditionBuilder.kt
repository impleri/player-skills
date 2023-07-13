package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.mobs.MobConditions
import net.impleri.playerskills.mobs.MobRestriction
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player

class MobRestrictionConditionBuilder :
  AbstractRestrictionConditionBuilder<EntityType<*>, MobRestriction>(),
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
    spawnMode = parseSpawnMode(jsonElement)
    usable = parseValue(jsonElement, "usable", { it.asBoolean }, true)
    val (include, exclude) = parseSpawners(jsonElement)
    includeSpawners = include.toMutableList()
    excludeSpawners = exclude.toMutableList()
  }

  private fun parseSpawnMode(raw: JsonObject): EntitySpawnMode {
    val spawnable = parseValue(raw, "spawnable", { it.asBoolean }) ?: false
    val all = parseValue(raw, "allPlayers", { it.asBoolean }) ?: false
    val always = parseValue(raw, "always", { it.asBoolean }) ?: false

    return when {
      (spawnable && always) -> EntitySpawnMode.ALLOW_ALWAYS
      (spawnable && all) -> EntitySpawnMode.ALLOW_IF_ALL_MATCH
      spawnable -> EntitySpawnMode.ALLOW_IF_ANY_MATCH
      always -> EntitySpawnMode.DENY_ALWAYS
      all -> EntitySpawnMode.DENY_IF_ALL_MATCH
      else -> EntitySpawnMode.DENY_IF_ANY_MATCH
    }
  }

  private fun parseSpawners(raw: JsonObject): Pair<List<MobSpawnType>, List<MobSpawnType>> {
    return getValue(raw, "spawners")?.let {
      if (it.isJsonObject) {
        (
          parseInclude(it, this::ifSpawner) to parseExclude(
            it,
            this::ifSpawner,
          )
          )
      } else {
        null
      }
    } ?: (ArrayList<MobSpawnType>() to ArrayList())
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
