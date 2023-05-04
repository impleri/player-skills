package net.impleri.playerskills.integrations.kubejs.api

import dev.latvian.mods.kubejs.server.ServerEventJS
import dev.latvian.mods.kubejs.util.ConsoleJS
import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey

abstract class AbstractRegistrationEventJS<T, R : AbstractRestriction<T>, B : AbstractRestrictionBuilder<R>>(
  s: MinecraftServer,
  private val type: String,
  private val registry: Registry<T>,
) : ServerEventJS(s) {
  private val registryName = registry.key() as ResourceKey<Registry<T>>

  fun restrict(resourceName: String, consumer: (B) -> Unit) {
    val registrationType = RegistrationType(resourceName, registryName)
    registrationType.ifNamespace { restrictNamespace(it, consumer) }
    registrationType.ifName { restrictOne(it, consumer) }
    registrationType.ifTag { restrictTag(it, consumer) }
  }

  @HideFromJS
  protected abstract fun restrictOne(name: ResourceLocation, consumer: (B) -> Unit)

  @HideFromJS
  protected fun logRestrictionCreation(restriction: R, name: ResourceLocation) {
    val inBiomes = appendListInfo(restriction.includeBiomes, "in biomes")
    val notInBiomes = appendListInfo(restriction.excludeBiomes, "not in biomes")
    val inDimensions = appendListInfo(restriction.includeDimensions, "in dimensions")
    val notInDimensions = appendListInfo(restriction.excludeDimensions, "not in dimensions")

    val details = listOf(inBiomes, notInBiomes, inDimensions, notInDimensions)
      .filter { it.isNotEmpty() }
      .joinToString(", ")

    ConsoleJS.SERVER.info("Created $type restriction for $name $details")
  }

  @HideFromJS
  private fun appendListInfo(list: List<ResourceLocation>, description: String): String {
    return if (list.isEmpty()) "" else "$description $list"
  }

  @HideFromJS
  private fun restrictNamespace(namespace: String, consumer: (B) -> Unit) {
    ConsoleJS.SERVER.info("Creating $type restrictions for namespace $namespace")

    registry.keySet()
      .filter { it.namespace == namespace }
      .forEach { restrictOne(it, consumer) }
  }

  @HideFromJS
  abstract fun isTagged(tag: TagKey<T>): (T) -> Boolean

  @HideFromJS
  abstract fun getName(resource: T): ResourceLocation

  @HideFromJS
  private fun restrictTag(tag: TagKey<T>, consumer: (B) -> Unit) {
    ConsoleJS.SERVER.info("Creating $type restrictions for tag ${tag.location}")
    registry
      .filter(isTagged(tag))
      .forEach { restrictOne(getName(it), consumer) }
  }
}
