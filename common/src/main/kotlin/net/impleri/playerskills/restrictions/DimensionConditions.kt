package net.impleri.playerskills.restrictions

import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer

interface DimensionConditions<Target, Restriction : AbstractRestriction<Target>> {
  val server: MinecraftServer

  val includeDimensions: MutableList<ResourceLocation>
  val excludeDimensions: MutableList<ResourceLocation>

  private fun ifInDimensionsNamespaced(namespace: String, callback: (ResourceLocation) -> Unit) {
    server.levelKeys()
      .asSequence()
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

  fun inDimension(dimension: String): DimensionConditions<Target, Restriction> {
    ifDimension(dimension) { includeDimensions.add(it) }

    return this
  }

  fun notInDimension(dimension: String): DimensionConditions<Target, Restriction> {
    ifDimension(dimension) { excludeDimensions.add(it) }

    return this
  }
}
