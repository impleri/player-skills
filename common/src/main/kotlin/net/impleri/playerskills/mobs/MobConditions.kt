package net.impleri.playerskills.mobs

import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.api.MobRestrictions
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import java.util.function.Predicate

interface MobConditions<Player> : RestrictionConditionsBuilder<EntityType<*>, Player, MobRestriction> {
  var replacement: EntityType<*>?
  var spawnMode: EntitySpawnMode
  var usable: Boolean?
  var includeSpawners: MutableList<MobSpawnType>
  var excludeSpawners: MutableList<MobSpawnType>

  override fun unless(predicate: Predicate<Player>): MobConditions<Player> {
    when (spawnMode) {
      EntitySpawnMode.ALLOW_IF_ANY_MATCH -> {
        spawnMode = EntitySpawnMode.ALLOW_UNLESS_ANY_MATCH
        condition(predicate)
      }

      EntitySpawnMode.ALLOW_IF_ALL_MATCH -> {
        spawnMode = EntitySpawnMode.ALLOW_UNLESS_ALL_MATCH
        condition(predicate)
      }

      EntitySpawnMode.DENY_IF_ANY_MATCH -> {
        spawnMode = EntitySpawnMode.DENY_UNLESS_ANY_MATCH
        condition(predicate)
      }

      EntitySpawnMode.DENY_IF_ALL_MATCH -> {
        spawnMode = EntitySpawnMode.DENY_UNLESS_ALL_MATCH
        condition(predicate)
      }

      else -> super.unless(predicate)
    }

    return this
  }

  fun always(): MobConditions<Player> {
    spawnMode = when (spawnMode) {
      EntitySpawnMode.ALLOW_ALWAYS, EntitySpawnMode.ALLOW_IF_ANY_MATCH, EntitySpawnMode.ALLOW_UNLESS_ANY_MATCH, EntitySpawnMode.ALLOW_IF_ALL_MATCH, EntitySpawnMode.ALLOW_UNLESS_ALL_MATCH ->
        EntitySpawnMode.ALLOW_ALWAYS

      EntitySpawnMode.DENY_ALWAYS, EntitySpawnMode.DENY_IF_ANY_MATCH, EntitySpawnMode.DENY_UNLESS_ANY_MATCH, EntitySpawnMode.DENY_IF_ALL_MATCH, EntitySpawnMode.DENY_UNLESS_ALL_MATCH ->
        EntitySpawnMode.DENY_ALWAYS
    }

    return this
  }

  fun spawnable(requireAll: Boolean? = false): MobConditions<Player> {
    spawnMode =
      if (requireAll == true) EntitySpawnMode.ALLOW_IF_ALL_MATCH else EntitySpawnMode.ALLOW_IF_ANY_MATCH

    return this
  }

  fun unspawnable(requireAll: Boolean? = false): MobConditions<Player> {
    spawnMode =
      if (requireAll == true) EntitySpawnMode.DENY_IF_ALL_MATCH else EntitySpawnMode.DENY_IF_ANY_MATCH

    return this
  }

  fun fromSpawner(spawner: MobSpawnType): MobConditions<Player> {
    includeSpawners.add(spawner)
    excludeSpawners.remove(spawner)

    return this
  }

  fun fromSpawner(spawner: String): MobConditions<Player> {
    val spawnType = MobRestrictions.spawnTypeMap[spawner]

    if (spawnType == null) {
      MobSkills.LOGGER.warn("Could not find spawn type named $spawner")
    } else {
      fromSpawner(spawnType)
    }

    return this
  }

  fun notFromSpawner(spawner: MobSpawnType): MobConditions<Player> {
    excludeSpawners.add(spawner)
    includeSpawners.remove(spawner)

    return this
  }

  fun notFromSpawner(spawner: String): MobConditions<Player> {
    val spawnType = MobRestrictions.spawnTypeMap[spawner]

    if (spawnType == null) {
      MobSkills.LOGGER.warn("Could not find spawn type named $spawner")
    } else {
      notFromSpawner(spawnType)
    }

    return this
  }

  fun usable(): MobConditions<Player> {
    usable = true

    return this
  }

  fun unusable(): MobConditions<Player> {
    usable = false

    return this
  }

  fun nothing(): MobConditions<Player> {
    usable = true
    spawnMode = EntitySpawnMode.ALLOW_ALWAYS

    return this
  }

  fun everything(): MobConditions<Player> {
    usable = false
    spawnMode = EntitySpawnMode.DENY_ALWAYS

    return this
  }
}
