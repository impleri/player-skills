package net.impleri.playerskills.restrictions

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

abstract class AbstractRestrictionBuilder<Target, Restriction : AbstractRestriction<Target>>(
  protected val registry: Registry<Target>,
  private val givenLogger: PlayerSkillsLogger? = null,
) {
  protected val logger: PlayerSkillsLogger
    get() = givenLogger ?: PlayerSkills.LOGGER

  @Suppress("UNCHECKED_CAST")
  private val registryName = registry.key() as ResourceKey<Registry<Target>>

  /**
   * Register a Restriction using a Builder consumed in a script (e.g. CraftTweaker, KubeJS)
   */
  fun <Player> create(resourceName: String, builder: RestrictionConditionsBuilder<Target, Player, Restriction>) {
    val registrationType = RegistrationType(resourceName, registryName)
    registrationType.ifNamespace { restrictNamespace(it, builder) }
    registrationType.ifName { restrictOne(it, builder) }
    registrationType.ifTag { restrictTag(it, builder) }
  }

  /**
   * Type-specific handler for creating a restriction using data from a Builder
   */
  protected abstract fun <Player> restrictOne(
    name: ResourceLocation,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  )

  /**
   * Internal, reusable helper to get all registered entries from the given namespace and create a restriction for them
   */
  private fun <Player> restrictNamespace(
    namespace: String,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  ) {
    registry.keySet()
      .asSequence()
      .filter { it.namespace == namespace }
      .forEach { restrictOne(it, builder) }
  }

  /**
   * Type-specific helper to create a predicate determining if a given Target matches the given TagKey
   */
  protected abstract fun isTagged(tag: TagKey<Target>): (Target) -> Boolean

  /**
   * Type-specific helper to retrieve the ResourceLocation related to the given Target
   */
  protected abstract fun getName(resource: Target): ResourceLocation

  /**
   * Internal, reusable helper to get all registered entries with the given tag and create a restriction for them
   */
  private fun <Player> restrictTag(
    tag: TagKey<Target>,
    builder: RestrictionConditionsBuilder<Target, Player, Restriction>,
  ) {
    registry
      .filter(isTagged(tag))
      .asSequence()
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
