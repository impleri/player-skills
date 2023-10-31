package net.impleri.playerskills.facades

import dev.architectury.registry.registries.Registrar
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

class ArchitecturyRegistrar[T](private val registrar: Option[Registrar[T]]) {
  def entries(): Map[ResourceKey[T], T] = {
    registrar.map(_.entrySet())
      .map(CollectionConverters.asScala(_))
      .map(_.map(e => (e.getKey, e.getValue)))
      .map(_.toMap)
      .getOrElse(Map.empty)
  }
}

object ArchitecturyRegistrar {
  def apply[T](registrar: Option[Registrar[T]]): ArchitecturyRegistrar[T] = new ArchitecturyRegistrar(registrar)

  def apply[T](key: ResourceLocation): ArchitecturyRegistrar[T] = {
    apply[T](Registries.get(PlayerSkills.MOD_ID)
      .builder(key)
      .build()
      .asInstanceOf[Registrar[T]]
      .pipe(Option.apply),
    )
  }
}
