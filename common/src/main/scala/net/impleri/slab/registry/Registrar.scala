package net.impleri.slab.registry

import dev.architectury.registry.registries.{Registrar => ArchRegistrar}
import dev.architectury.registry.registries.Registries
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.resources.ResourceKey

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class Registrar[T](private val underlying: Option[ArchRegistrar[T]]) {
  def entries(): Map[ResourceKey[T], T] = {
    underlying.map(_.entrySet())
      .map(_.asScala)
      .map(_.map(e => (e.getKey, e.getValue)))
      .map(_.toMap)
      .getOrElse(Map.empty)
  }
}

object Registrar {
  def apply[T](registrar: Option[ArchRegistrar[T]]): Registrar[T] = new Registrar(registrar)

  def apply[T](key: ResourceLocation, modName: String): Registrar[T] = {
    apply[T](Registries.get(modName)
      .builder(key.value)
      .build()
      .asInstanceOf[ArchRegistrar[T]]
      .pipe(Option.apply),
    )
  }
}
