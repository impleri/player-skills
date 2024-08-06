package net.impleri.slab.registry

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.slab.resources.Registerable
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class DeferredRegistry[T <: Registerable](
  private val underlying: DeferredRegistry.Vanilla[T],
) {
  def register(name: ResourceLocation, value: T): Unit = {
    underlying.register(name.value, () => value)
  }

  def commit(): Unit = underlying.register()
}

object DeferredRegistry {
  type Vanilla[T] = DeferredRegister[T]

  def apply[T <: Registerable](
    modId: String,
    key: ResourceKey.Registry[T],
  ): DeferredRegistry[T] = {
    DeferredRegister
      .create(modId, key.value)
      .pipe(DeferredRegistry(_))
  }
}
