package net.impleri.playerskills.mobs

import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player

open class MobRestriction(
  entity: EntityType<*>,
  condition: (Player) -> Boolean,
  includeDimensions: List<ResourceLocation>? = null,
  excludeDimensions: List<ResourceLocation>? = null,
  includeBiomes: List<ResourceLocation>? = null,
  excludeBiomes: List<ResourceLocation>? = null,
  spawnMode: EntitySpawnMode? = null,
  usable: Boolean? = null,
  includeSpawners: List<MobSpawnType>? = null,
  excludeSpawners: List<MobSpawnType>? = null,
  replacement: EntityType<*>? = null,
) : AbstractRestriction<EntityType<*>>(
  entity,
  condition,
  includeDimensions ?: ArrayList(),
  excludeDimensions ?: ArrayList(),
  includeBiomes ?: ArrayList(),
  excludeBiomes ?: ArrayList(),
  replacement,
) {
  val spawnMode = spawnMode ?: EntitySpawnMode.ALLOW_ALWAYS
  val usable = usable ?: false
  val includeSpawners = includeSpawners ?: ArrayList()
  val excludeSpawners = excludeSpawners ?: ArrayList()
}
