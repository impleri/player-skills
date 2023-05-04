package net.impleri.playerskills.integrations.kubejs.api

import dev.latvian.mods.kubejs.BuilderBase
import dev.latvian.mods.kubejs.util.ConsoleJS
import dev.latvian.mods.rhino.util.HideFromJS
import dev.latvian.mods.rhino.util.RemapForJS
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull

/**
 * In order to use
 *
 * @param <T>
</T> */
abstract class AbstractRestrictionBuilder<T : AbstractRestriction<*>>(
  id: ResourceLocation?,
  @HideFromJS protected val server: MinecraftServer,
) : BuilderBase<T>(id) {
  @HideFromJS
  var condition = { _: Player -> true }

  @HideFromJS
  val includeDimensions: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  val excludeDimensions: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  val includeBiomes: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  val excludeBiomes: MutableList<ResourceLocation> = ArrayList()

  @RemapForJS("if")
  fun condition(predicate: (PlayerDataJS) -> Boolean): AbstractRestrictionBuilder<T> {
    condition = { player: Player? -> player?.let { predicate(PlayerDataJS(player)) } ?: true }
    return this
  }

  fun unless(predicate: (PlayerDataJS) -> Boolean): AbstractRestrictionBuilder<T> {
    condition = { player: Player? -> player?.let { !predicate(PlayerDataJS(player)) } ?: true }
    return this
  }

  @HideFromJS
  private fun ifInDimensionsNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    server.levelKeys()
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  @HideFromJS
  private fun ifDimension(dimensionName: String, callback: (ResourceLocation) -> Unit) {
    val registrationType = RegistrationType(dimensionName, Registry.DIMENSION_REGISTRY)

    registrationType.ifNamespace { ifInDimensionsNamespaced(it, callback) }
    // Note: Dimensions do not have tags, so we cannot use the #tag selector on dimensions
    registrationType.ifName(callback)
  }

  fun inDimension(dimension: String): AbstractRestrictionBuilder<T> {
    ifDimension(dimension) { includeDimensions.add(it) }

    return this
  }

  fun notInDimension(dimension: String): AbstractRestrictionBuilder<T> {
    ifDimension(dimension) { excludeDimensions.add(it) }

    return this
  }

  @HideFromJS
  private fun ifInBiomesNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    BuiltinRegistries.BIOME.entrySet()
      .map { it.key }
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  @HideFromJS
  private fun ifInBiomesTagged(tag: TagKey<Biome>, callback: (ResourceLocation) -> Unit) {
    val values = server
      .registryAccess()
      .registry(Registry.BIOME_REGISTRY)
      .map { it.getTag(tag) }
      .getOrNull()

    if (values?.isEmpty != false) {
      ConsoleJS.SERVER.warn("No biomes found matching tag ${tag.location}")
      return
    }

    values.get()
      .map { it.unwrapKey() }
      .filter { it.isPresent }
      .map { it.get() }
      .map { it.location() }
      .forEach(callback)
  }

  @HideFromJS
  private fun ifBiome(dimensionName: String, callback: (ResourceLocation) -> Unit) {
    val registrationType = RegistrationType(dimensionName, Registry.BIOME_REGISTRY)
    registrationType.ifNamespace { ifInBiomesNamespaced(it, callback) }
    registrationType.ifTag { ifInBiomesTagged(it, callback) }
    registrationType.ifName(callback)
  }

  fun inBiome(biome: String): AbstractRestrictionBuilder<T> {
    ifBiome(biome) { includeBiomes.add(it) }

    return this
  }

  fun notInBiome(biome: String): AbstractRestrictionBuilder<T> {
    ifBiome(biome) { excludeBiomes.add(it) }

    return this
  }
}
