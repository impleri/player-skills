package net.impleri.playerskills.api

import net.impleri.playerskills.mobs.MobConditions
import net.impleri.playerskills.mobs.MobRestriction
import net.impleri.playerskills.mobs.MobSkills
import net.impleri.playerskills.restrictions.AbstractRestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType

class MobRestrictionBuilder : AbstractRestrictionBuilder<EntityType<*>, MobRestriction>(
  Registry.ENTITY_TYPE,
  MobSkills.LOGGER,
) {
  override fun <Player> restrictOne(
    targetName: ResourceLocation,
    builder: RestrictionConditionsBuilder<EntityType<*>, Player, MobRestriction>,
  ) {
    builder as MobConditions<Player>

    val type = MobRestrictions.getValue(targetName)

    if (type == null) {
      logger.warn("Could not find any mob named $targetName")
      return
    }

    val restriction = MobRestriction(
      type,
      builder.actualCondition,
      builder.includeDimensions,
      builder.excludeDimensions,
      builder.includeBiomes,
      builder.excludeBiomes,
      builder.spawnMode,
      builder.usable,
      builder.includeSpawners,
      builder.excludeSpawners,
      builder.replacement,
    )

    MobRestrictions.add(builder.name ?: targetName, restriction)
    logRestriction(targetName, restriction)
  }

  override fun isTagged(target: EntityType<*>, tag: TagKey<EntityType<*>>): Boolean {
    return target.`is`(tag)
  }

  override fun getName(target: EntityType<*>): ResourceLocation {
    return MobRestrictions.getName(target)
  }

  companion object {
    private val instance = MobRestrictionBuilder()

    fun <Player> register(name: String, builder: MobConditions<Player>) {
      instance.create(name, builder)
    }

    fun register() {
      instance.register()
    }
  }
}
