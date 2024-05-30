package net.impleri.slab.registry

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class DeferredRegistry[T](private val underlying: DeferredRegister[T]) {
  def register(name: ResourceLocation, value: T): Unit = {
    underlying.register(name.value, () => value)
  }

  def commit(): Unit = underlying.register()
}

object DeferredRegistry {
  def apply[T](modId: String, key: RegistryKey[T]): DeferredRegistry[T] = {
    DeferredRegister
      .create(modId, key.value)
      .pipe(DeferredRegistry(_))
  }
}
