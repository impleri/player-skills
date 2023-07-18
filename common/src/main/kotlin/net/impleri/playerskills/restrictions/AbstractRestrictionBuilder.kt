package net.impleri.playerskills.restrictions

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player

abstract class AbstractRestrictionBuilder<Target, Restriction : AbstractRestriction<Target>>(
  protected val registry: Registry<Target>,
  protected val logger: PlayerSkillsLogger = PlayerSkills.LOGGER,
) {
  @Suppress("UNCHECKED_CAST")
  private val registryName = registry.key() as ResourceKey<Registry<Target>>
  private val restrictions: MutableMap<String, RestrictionConditionsBuilder<Target, *, Restriction>> =
    HashMap()

  fun <Player> create(restrictionName: String, builder: RestrictionConditionsBuilder<Target, Player, Restriction>) {
    restrictions[restrictionName] = builder
  }

  fun register() {
    restrictions.entries.forEach { (name, builder) -> restrict(name, builder) }

    restrictions.clear()
  }

  /**
   * Register a Restriction using a Builder consumed in a script (e.g. CraftTweaker, KubeJS)
   */
  private fun <Player> restrict(
    resourceName: String,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  ) {
    val registrationType = RegistrationType(resourceName, registryName)
    registrationType.ifNamespace { restrictNamespace(it, builder) }
    registrationType.ifName { restrictOne(it, builder) }
    registrationType.ifTag { restrictTag(it, builder) }
  }

  /**
   * Type-specific handler for creating a restriction using data from a Builder
   */
  protected abstract fun <Player> restrictOne(
    targetName: ResourceLocation,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  )

  /**
   * Internal, reusable helper to get all registered entries from the given namespace and create a restriction for them
   */
  private fun <Player> restrictNamespace(
    namespace: String,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  ) {
    logger.info("Creating restriction for $namespace namespace")
    registry.keySet()
      .asSequence()
      .filter { it.namespace == namespace }
      .forEach { restrictOne(it, builder) }
  }

  /**
   * Type-specific helper to create a predicate determining if a given Target matches the given TagKey
   */
  protected abstract fun isTagged(target: Target, tag: TagKey<Target>): Boolean

  /**
   * Type-specific helper to retrieve the ResourceLocation related to the given Target
   */
  protected abstract fun getName(target: Target): ResourceLocation

  /**
   * Internal, reusable helper to get all registered entries with the given tag and create a restriction for them
   */
  private fun <Player> restrictTag(
    tag: TagKey<Target>,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  ) {
    logger.info("Creating restriction for ${tag.location} tag")

    registry.asSequence()
      .filter { isTagged(it, tag) }
      .map { getName(it) }
      .forEach { restrictOne(it, builder) }
  }

  protected fun logRestriction(name: ResourceLocation, restriction: Restriction) {
    val inBiomes = appendListInfo(restriction.includeBiomes, "in biomes")
    val notInBiomes = appendListInfo(restriction.excludeBiomes, "not in biomes")
    val inDimensions = appendListInfo(restriction.includeDimensions, "in dimensions")
    val notInDimensions = appendListInfo(restriction.excludeDimensions, "not in dimensions")

    val details = listOf(inBiomes, notInBiomes, inDimensions, notInDimensions)
      .filter { it.isNotEmpty() }
      .joinToString(", ")

    logger.info("Created restriction for $name $details")
  }

  private fun appendListInfo(list: List<ResourceLocation>, description: String): String {
    return if (list.isEmpty()) "" else "$description $list"
  }
}
