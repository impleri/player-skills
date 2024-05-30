package net.impleri.slab.registry

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.world.Biome
import net.minecraft.core.Registry
import net.minecraft.resources.{ResourceKey => McResourceKey}

import scala.util.chaining.scalaUtilChainingOps

case class RegistryKey[T](private val underlying: McResourceKey[Registry[T]]) {
  def value: McResourceKey[Registry[T]] = underlying
}

object RegistryKey {
  def apply[T](resource: ResourceLocation): RegistryKey[T] = {
    McResourceKey.createRegistryKey[T](resource.value)
      .pipe(RegistryKey(_))
  }

  lazy val Biome: RegistryKey[Biome] = apply(Registry.BIOME_REGISTRY)
}
