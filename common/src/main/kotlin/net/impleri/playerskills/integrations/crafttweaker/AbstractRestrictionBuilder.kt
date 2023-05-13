package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.biome.Biome
import org.openzen.zencode.java.ZenCodeType
import kotlin.jvm.optionals.getOrNull

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.AbstractRestrictionBuilder")
abstract class AbstractRestrictionBuilder<T : AbstractRestriction<*>>(
  val id: ResourceLocation,
  protected val server: MinecraftServer,
) {
  var condition = { _: Player -> true }

  val includeDimensions: MutableList<ResourceLocation> = ArrayList()
  val excludeDimensions: MutableList<ResourceLocation> = ArrayList()

  val includeBiomes: MutableList<ResourceLocation> = ArrayList()
  val excludeBiomes: MutableList<ResourceLocation> = ArrayList()

  fun condition(predicate: (Player) -> Boolean): AbstractRestrictionBuilder<T> {
    condition = { player: Player? -> player?.let { predicate(it) } ?: true }
    return this
  }

  fun unless(predicate: (Player) -> Boolean): AbstractRestrictionBuilder<T> {
    condition = { player: Player? -> player?.let { !predicate(it) } ?: true }
    return this
  }

  private fun ifInDimensionsNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    server.levelKeys()
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  private fun ifDimension(dimensionName: String, callback: (ResourceLocation) -> Unit) {
    val registrationType = RegistrationType(dimensionName, Registry.DIMENSION_REGISTRY)

    registrationType.ifNamespace { ifInDimensionsNamespaced(it, callback) }
    // Note: Dimensions do not have tags, so we cannot use the #tag selector on dimensions
    registrationType.ifName(callback)
  }

  @ZenCodeType.Method
  fun inDimension(dimension: String): AbstractRestrictionBuilder<T> {
    ifDimension(dimension) { includeDimensions.add(it) }

    return this
  }

  @ZenCodeType.Method
  fun notInDimension(dimension: String): AbstractRestrictionBuilder<T> {
    ifDimension(dimension) { excludeDimensions.add(it) }

    return this
  }

  private fun ifInBiomesNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    BuiltinRegistries.BIOME.entrySet()
      .map { it.key }
      .map { it.location() }
      .filter { it.namespace == namespace }
      .forEach(callback)
  }

  private fun ifInBiomesTagged(tag: TagKey<Biome>, callback: (ResourceLocation) -> Unit) {
    val values = server
      .registryAccess()
      .registry(Registry.BIOME_REGISTRY)
      .map { it.getTag(tag) }
      .getOrNull()

    if (values?.isEmpty != false) {
      return
    }

    values.get()
      .map { it.unwrapKey() }
      .filter { it.isPresent }
      .map { it.get() }
      .map { it.location() }
      .forEach(callback)
  }

  private fun ifBiome(dimensionName: String, callback: (ResourceLocation) -> Unit) {
    val registrationType = RegistrationType(dimensionName, Registry.BIOME_REGISTRY)
    registrationType.ifNamespace { ifInBiomesNamespaced(it, callback) }
    registrationType.ifTag { ifInBiomesTagged(it, callback) }
    registrationType.ifName(callback)
  }

  @ZenCodeType.Method
  fun inBiome(biome: String): AbstractRestrictionBuilder<T> {
    ifBiome(biome) { includeBiomes.add(it) }

    return this
  }

  @ZenCodeType.Method
  fun notInBiome(biome: String): AbstractRestrictionBuilder<T> {
    ifBiome(biome) { excludeBiomes.add(it) }

    return this
  }
}
