package net.impleri.playerskills.restrictions

import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.OptionalInt

open class Registry<T : AbstractRestriction<*>> {
  private val registryKey: ResourceLocation =
    net.impleri.playerskills.utils.SkillResourceLocation.of("mob_restrictions")
  private val registryResource: ResourceKey<Registry<List<T>>> = ResourceKey.createRegistryKey(registryKey)

  protected val registry: MappedRegistry<List<T>> = MappedRegistry(registryResource, Lifecycle.stable(), null)

  fun entries(): List<T> {
    return registry.flatten()
  }

  fun find(name: ResourceLocation): List<T> {
    return registry[name] ?: ArrayList()
  }

  fun add(name: ResourceLocation, restriction: T) {
    val restrictions = find(name).toMutableList()
    restrictions.add(restriction)

    registry.registerOrOverride(
      OptionalInt.empty(),
      ResourceKey.create(registryResource, name),
      restrictions.toList(),
      Lifecycle.stable(),
    )
  }
}
