package net.impleri.playerskills.restrictions

import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull

interface BiomeConditions<Target, Restriction : AbstractRestriction<Target>> {
  val server: MinecraftServer
  val includeBiomes: MutableList<ResourceLocation>
  val excludeBiomes: MutableList<ResourceLocation>

  private fun ifInBiomesNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    server
      .registryAccess()
      .registry(Registry.BIOME_REGISTRY)
      .getOrNull()
      ?.entrySet()
      ?.asSequence()
      ?.map { it.key }
      ?.map { it.location() }
      ?.filter { it.namespace == namespace }
      ?.forEach(callback)
  }

  private fun ifInBiomesTagged(tag: TagKey<Biome>, callback: (ResourceLocation) -> Unit) {
    server.registryAccess()
      .registry(Registry.BIOME_REGISTRY)
      .getOrNull()
      ?.getTag(tag)
      ?.getOrNull()
      ?.asSequence()
      ?.map { it.unwrapKey() }
      ?.map { it.getOrNull() }
      ?.map { it?.location() }
      ?.forEach { it?.let(callback) }
  }

  private fun ifBiome(dimensionName: String, callback: (ResourceLocation) -> Unit) {
    val registrationType = RegistrationType(dimensionName, Registry.BIOME_REGISTRY)
    registrationType.ifNamespace { ifInBiomesNamespaced(it, callback) }
    registrationType.ifTag { ifInBiomesTagged(it, callback) }
    registrationType.ifName(callback)
  }

  fun inBiome(biome: String): BiomeConditions<Target, Restriction> {
    ifBiome(biome) { includeBiomes.add(it) }

    return this
  }

  fun notInBiome(biome: String): BiomeConditions<Target, Restriction> {
    ifBiome(biome) { excludeBiomes.add(it) }

    return this
  }
}
