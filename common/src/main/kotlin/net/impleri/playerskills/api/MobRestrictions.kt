package net.impleri.playerskills.api

import net.impleri.playerskills.mobs.MobRestriction
import net.impleri.playerskills.mobs.MobSkills
import net.impleri.playerskills.restrictions.RestrictionsApi
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.phys.Vec3
import java.lang.reflect.Field
import kotlin.math.sqrt
import net.impleri.playerskills.restrictions.Registry as RestrictionsRegistry

class MobRestrictions private constructor(
  registry: RestrictionsRegistry<MobRestriction>,
  fields: Array<Field>,
) : RestrictionsApi<EntityType<*>, MobRestriction>(registry, fields, MobSkills.LOGGER) {
  override fun getTargetName(target: EntityType<*>): ResourceLocation {
    return getName(target)
  }

  override fun createPredicateFor(target: EntityType<*>): (EntityType<*>) -> Boolean {
    return { target == it }
  }

  fun isUsable(player: Player?, entity: EntityType<*>): Boolean {
    player ?: return DEFAULT_CAN_RESPONSE

    val level = player.getLevel()
    val dimension = level.dimension().location()
    val biome = level.getBiome(player.onPos).unwrapKey().orElseThrow().location()

    return canPlayer(player, entity, dimension, biome, null, "usable")
  }

  private val entityRestrictionsCache = HashMap<EntityType<*>, List<MobRestriction>>()
  private fun populateEntityRestrictions(entity: EntityType<*>): List<MobRestriction> {
    val isTargetingEntity = createPredicateFor(entity)
    return registry.entries()
      .filter { isTargetingEntity(it.target) }
  }

  private fun getRestrictionsFor(entity: EntityType<*>): List<MobRestriction> {
    return entityRestrictionsCache.computeIfAbsent(entity) { populateEntityRestrictions(it) }
  }

  fun canSpawnAround(
    entity: EntityType<*>,
    players: List<Player>,
    dimension: ResourceLocation?,
    biome: ResourceLocation?,
    spawnType: MobSpawnType?,
  ): Boolean {
    val isTargetingNearbyPlayers = createExtraFilter(players)
    return getRestrictionsFor(entity)
      .asSequence()
      .filter(inIncludedDimension(dimension))
      .filter(notInExcludedDimension(dimension))
      .filter(inIncludedBiome(biome))
      .filter(notInExcludedBiome(biome))
      .filter(inIncludedSpawner(spawnType))
      .filter(notInExcludedSpawner(spawnType))
      .filter(isTargetingNearbyPlayers)
      .toList()
      .isEmpty()
  }

  private fun inIncludedSpawner(spawnType: MobSpawnType?): (MobRestriction) -> Boolean {
    return { it.includeSpawners.isEmpty() || it.includeSpawners.contains(spawnType) }
  }

  private fun notInExcludedSpawner(spawnType: MobSpawnType?): (MobRestriction) -> Boolean {
    return { it.excludeSpawners.isEmpty() || !it.excludeSpawners.contains(spawnType) }
  }

  private fun createExtraFilter(players: List<Player>): (MobRestriction) -> Boolean {
    return { !doesRestrictionAllowSpawn(it, players) }
  }

  companion object {
    internal val RestrictionRegistry = RestrictionsRegistry<MobRestriction>()

    private val allRestrictionFields = MobRestrictions::class.java.declaredFields

    internal val INSTANCE = MobRestrictions(RestrictionRegistry, allRestrictionFields)

    val spawnTypeMap: Map<String, MobSpawnType> = mapOf(
      "natural" to MobSpawnType.NATURAL,
      "chunk" to MobSpawnType.CHUNK_GENERATION,
      "spawner" to MobSpawnType.SPAWNER,
      "structure" to MobSpawnType.STRUCTURE,
      "patrol" to MobSpawnType.PATROL,
    )

    fun add(name: ResourceLocation, restriction: MobRestriction) {
      RestrictionRegistry.add(name, restriction)
    }

    fun getName(type: EntityType<*>): ResourceLocation {
      return EntityType.getKey(type)
    }

    fun getValue(type: ResourceLocation): EntityType<*>? {
      val entityType = Registry.ENTITY_TYPE[type]

      return if (isDefaultValue(entityType, type)) null else entityType
    }

    fun isDefaultValue(entityType: EntityType<*>, target: ResourceLocation? = null): Boolean {
      // Currently, the default entity type is minecraft:pig, so we're making sure that the restriction actually targets
      // a pig before returning that value
      val foundType = getName(entityType)

      val isDefaultType = Registry.ENTITY_TYPE.defaultKey == foundType

      val isTargetingDefaultType = target?.let { it == foundType } ?: false

      return isDefaultType && !isTargetingDefaultType
    }

    fun canInteractWith(entity: EntityType<*>, player: Player): Boolean {
      val usable = INSTANCE.isUsable(player, entity)

      MobSkills.LOGGER.debug("Can ${player.name} interact with ${getName(entity)}? $usable")

      return usable
    }

    fun canSpawn(
      entity: LivingEntity,
      levelAccessor: LevelAccessor,
      position: Vec3i,
      spawnType: MobSpawnType?,
    ): Boolean {
      val type = entity.type
      val spawnRadius = type.category.despawnDistance

      val dimension = entity.level.dimension().location()
      val biome = levelAccessor.getBiome(BlockPos(position)).unwrapKey().orElseThrow().location()

      val playersInRange = getNearbyPlayers(levelAccessor.players() as List<Player>, position, spawnRadius)

      MobSkills.LOGGER.debug("Found ${playersInRange.size} players within $spawnRadius blocks of ${position.toShortString()}")
      return INSTANCE.canSpawnAround(type, playersInRange, dimension, biome, spawnType)
    }

    private fun getNearbyPlayers(playersInDim: List<Player>, position: Vec3i, radius: Int): List<Player> {
      return playersInDim
        .filter { sqrt(it.distanceToSqr(Vec3.atCenterOf(position))) <= radius }
        .toList()
    }

    private fun doesRestrictionAllowSpawn(restriction: MobRestriction, players: List<Player>): Boolean {
      return when (restriction.spawnMode) {
        EntitySpawnMode.ALLOW_IF_ANY_MATCH, EntitySpawnMode.DENY_UNLESS_ANY_MATCH -> ifAny(
          players,
          restriction.condition,
        )

        EntitySpawnMode.ALLOW_IF_ALL_MATCH, EntitySpawnMode.DENY_UNLESS_ALL_MATCH -> ifAll(
          players,
          restriction.condition,
        )

        EntitySpawnMode.ALLOW_UNLESS_ANY_MATCH, EntitySpawnMode.DENY_IF_ANY_MATCH -> !ifAll(
          players,
          restriction.condition,
        )

        EntitySpawnMode.ALLOW_UNLESS_ALL_MATCH, EntitySpawnMode.DENY_IF_ALL_MATCH -> !ifAny(
          players,
          restriction.condition,
        )

        EntitySpawnMode.ALLOW_ALWAYS -> true
        EntitySpawnMode.DENY_ALWAYS -> false
      }
    }

    private fun ifAny(players: List<Player>, consumer: (Player) -> Boolean): Boolean {
      return players.any { consumer(it) }
    }

    private fun ifAll(players: List<Player>, consumer: (Player) -> Boolean): Boolean {
      return players.all { consumer(it) }
    }
  }
}
