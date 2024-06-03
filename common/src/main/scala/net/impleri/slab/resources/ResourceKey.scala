package net.impleri.slab.resources

import net.impleri.slab.registry.Registry
import net.impleri.slab.world.Biome
import net.minecraft.resources.{ResourceKey => McResourceKey}

import scala.util.chaining.scalaUtilChainingOps

case class ResourceKey[T](protected val underlying: ResourceKey.Vanilla[T]) {
  val name: Option[ResourceLocation] = Option(underlying.location()).flatMap(ResourceLocation(_))

  val value: ResourceKey.Vanilla[T] = underlying
}

object ResourceKey {
  type Any = ResourceKey[_]
  type Registry[T] = ResourceKey[Registry.Vanilla[T]]

  type Vanilla[T] = McResourceKey[T]
  type VanillaRegistry[T] = Vanilla[Registry.Vanilla[T]]
  type AnyVanilla = Vanilla[_]

  def forRegistry[T <: Registerable](resource: ResourceLocation): ResourceKey[Registry.Vanilla[T]] = {
    McResourceKey.createRegistryKey[T](resource.value)
      .pipe(new ResourceKey(_))
  }

  lazy val BIOME_REGISTRY: Registry[Biome.Vanilla] = ResourceKey(Registry.BIOME_REGISTRY)
}
