package net.impleri.playerskills.restrictions

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Predicate

abstract class RestrictionsApi<T, R : AbstractRestriction<T>>(
  protected val registry: Registry<R>,
  private val allRestrictionFields: Array<Field>,
  private val logger: PlayerSkillsLogger = PlayerSkills.LOGGER,
) {
  private val restrictionsCache: MutableMap<Player, List<R>> = ConcurrentHashMap()
  private val emptyFilter = { _: R -> true }

  @JvmRecord
  private data class ReplacementCacheKey<RT, RR : AbstractRestriction<RT>>(
    val target: RT,
    val dimension: ResourceLocation,
    val biome: ResourceLocation,
    val filter: Predicate<RR>,
  )

  private val replacementCache: MutableMap<Player, MutableMap<ReplacementCacheKey<T, R>, T>> = ConcurrentHashMap()

  init {
    SkillChangedEvent.EVENT.register(Consumer { clearPlayerCache(it) })
  }

  private fun clearPlayerCache(event: SkillChangedEvent<*>) {
    restrictionsCache.remove(event.player)
    replacementCache.remove(event.player)
  }

  private fun getField(name: String): Field? {
    return allRestrictionFields.firstOrNull { it.name == name }
  }

  private fun getFieldValueFor(restriction: R, fieldName: String): Boolean {
    try {
      // return the boolean value of the field
      return getField(fieldName)?.getBoolean(restriction) ?: true
    } catch (ignored: IllegalAccessException) {
    } catch (ignored: IllegalArgumentException) {
    } catch (ignored: NullPointerException) {
    } catch (ignored: ExceptionInInitializerError) {
    }

    // default to allow if we get some error when trying to access the field
    return true
  }

  /**
   * Determine the ResourceLocation of the target
   */
  protected abstract fun getTargetName(target: T): ResourceLocation

  /**
   * Create a Predicate using target to match with potential replacements registered to applicable restrictions.
   */
  protected abstract fun createPredicateFor(target: T): (T) -> Boolean
  private fun hasTarget(): (R) -> Boolean {
    return { it.target != null }
  }

  private fun matchesPlayer(player: Player): (R) -> Boolean {
    return { it.condition(player) }
  }

  private fun matchesTarget(target: T): (R) -> Boolean {
    val isMatchingTarget = createPredicateFor(target)

    return { isMatchingTarget(it.target) }
  }

  protected fun inIncludedDimension(dimension: ResourceLocation?): (R) -> Boolean {
    return { it.includeDimensions.isEmpty() || it.includeDimensions.contains(dimension) }
  }

  protected fun notInExcludedDimension(dimension: ResourceLocation?): (R) -> Boolean {
    return { it.excludeDimensions.isEmpty() || !it.excludeDimensions.contains(dimension) }
  }

  protected fun inIncludedBiome(biome: ResourceLocation?): (R) -> Boolean {
    return { it.includeBiomes.isEmpty() || it.includeBiomes.contains(biome) }
  }

  protected fun notInExcludedBiome(biome: ResourceLocation?): (R) -> Boolean {
    return { it.excludeBiomes.isEmpty() || !it.excludeBiomes.contains(biome) }
  }

  private fun populatePlayerRestrictions(player: Player): List<R> {
    return registry.entries().asSequence()
      .filter(hasTarget())
      .filter(matchesPlayer(player))
      .toList()
  }

  private fun getRestrictionsFor(player: Player): List<R> {
    return restrictionsCache.computeIfAbsent(player) { populatePlayerRestrictions(it) }
  }

  private fun getRestrictionsFor(
    player: Player,
    target: T,
    dimension: ResourceLocation,
    biome: ResourceLocation,
    filter: (R) -> Boolean,
  ): Sequence<R> {
    return getRestrictionsFor(player).asSequence()
      .filter(matchesTarget(target))
      .filter(inIncludedDimension(dimension))
      .filter(notInExcludedDimension(dimension))
      .filter(inIncludedBiome(biome))
      .filter(notInExcludedBiome(biome))
      .filter(filter)
  }

  private fun getReplacementsFor(player: Player): Sequence<R> {
    return getRestrictionsFor(player).asSequence()
      .filter { it.replacement != null }
  }

  private fun getReplacementsFor(
    player: Player,
    target: T,
    dimension: ResourceLocation,
    biome: ResourceLocation,
    filter: (R) -> Boolean,
  ): Sequence<R> {
    return getRestrictionsFor(player, target, dimension, biome, filter)
      .filter { it.replacement != null }
  }

  private fun getActualReplacement(
    player: Player,
    target: T,
    dimension: ResourceLocation,
    biome: ResourceLocation,
    filter: (R) -> Boolean,
  ): T {
    var replacement = target

    // Recurse through replacements until we don't have one so that we can allow for cascading replacements
    while (true) {
      val nextReplacement = getReplacementsFor(player, replacement, dimension, biome, filter)
        .map { it.replacement }
        .firstOrNull()

      replacement = nextReplacement ?: return replacement
    }
  }

  /**
   * Internal API used by ClientApi
   */
  @ApiStatus.Internal
  fun getFiltered(player: Player, predicate: (R) -> Boolean): List<R> {
    return getRestrictionsFor(player)
      .filter(predicate)
  }

  /**
   * Gets a count for all restrictions with replacements applicable to player.
   */
  fun countReplacementsFor(player: Player): Long {
    return getReplacementsFor(player).count().toLong()
  }

  /**
   * Get replacement for target using restrictions applicable to player in the current dimension and biome. Will return target if no replacements found.
   */
  @JvmOverloads
  fun getReplacementFor(
    player: Player,
    target: T,
    dimension: ResourceLocation,
    biome: ResourceLocation,
    filter: ((R) -> Boolean)? = null,
  ): T {
    val playerCache = replacementCache.computeIfAbsent(player) { _: Player -> HashMap() }

    val actualFilter = filter ?: emptyFilter
    val cacheKey = ReplacementCacheKey(target, dimension, biome, actualFilter)

    if (playerCache.containsKey(cacheKey)) {
      return playerCache[cacheKey]!!
    }

    val replacement = getActualReplacement(player, target, dimension, biome, actualFilter)
    logger.debug("${getTargetName(target)} should be cached as ${getTargetName(replacement)} in $dimension/$biome for ${player.name}")

    playerCache[cacheKey] = replacement
    replacementCache[player] = playerCache

    return replacement
  }

  @JvmOverloads
  fun canPlayer(
    player: Player?,
    target: T,
    dimension: ResourceLocation,
    biome: ResourceLocation,
    filter: ((R) -> Boolean)? = null,
    fieldName: String,
    resource: ResourceLocation? = getTargetName(target),
  ): Boolean {
    if (player == null) {
      logger.warn("Attempted to determine if null player can $fieldName on target $resource in $dimension/$biome")
      return false
    }

    val actualFilter = filter ?: emptyFilter

    val hasRestrictions = getRestrictionsFor(player, target, dimension, biome, actualFilter)
      .map { getFieldValueFor(it, fieldName) } // get field value
      .any { !it } // do we have any restrictions that deny the action

    logger.debug("Does ${player.name}  have $fieldName restrictions with $resource in  $dimension/$biome? $hasRestrictions")

    return !hasRestrictions
  }
}
