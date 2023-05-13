package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.utils.RegistrationType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import org.openzen.zencode.java.ZenCodeType

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.AbstractRegistrations")
abstract class AbstractRegistrations<T, R : AbstractRestriction<T>, B : AbstractRestrictionBuilder<R>>(
  val server: MinecraftServer,
  private val registry: Registry<T>,
) {
  @Suppress("UNCHECKED_CAST")
  private val registryName = registry.key() as ResourceKey<Registry<T>>

  protected fun createRestriction(resourceName: String, consumer: (B) -> Unit) {
    val registrationType = RegistrationType(resourceName, registryName)
    registrationType.ifNamespace { restrictNamespace(it, consumer) }
    registrationType.ifName { restrictOne(it, consumer) }
    registrationType.ifTag { restrictTag(it, consumer) }
  }

  protected abstract fun restrictOne(name: ResourceLocation, consumer: (B) -> Unit)

  private fun restrictNamespace(namespace: String, consumer: (B) -> Unit) {
    registry.keySet()
      .filter { it.namespace == namespace }
      .forEach { restrictOne(it, consumer) }
  }

  abstract fun isTagged(tag: TagKey<T>): (T) -> Boolean

  abstract fun getName(resource: T): ResourceLocation

  private fun restrictTag(tag: TagKey<T>, consumer: (B) -> Unit) {
    registry
      .filter(isTagged(tag))
      .forEach { restrictOne(getName(it), consumer) }
  }
}
