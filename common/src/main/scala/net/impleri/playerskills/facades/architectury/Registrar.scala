package net.impleri.playerskills.facades.architectury

import dev.architectury.registry.registries.{Registrar => ArchRegistrar}
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class Registrar[T](private val registrar: Option[ArchRegistrar[T]]) {
  def entries(): Map[ResourceKey[T], T] = {
    registrar.map(_.entrySet())
      .map(_.asScala)
      .map(_.map(e => (e.getKey, e.getValue)))
      .map(_.toMap)
      .getOrElse(Map.empty)
  }
}

object Registrar {
  def apply[T](registrar: Option[ArchRegistrar[T]]): Registrar[T] = new Registrar(registrar)

  def apply[T](key: ResourceLocation): Registrar[T] = {
    apply[T](Registries.get(PlayerSkills.MOD_ID)
      .builder(key)
      .build()
      .asInstanceOf[ArchRegistrar[T]]
      .pipe(Option.apply),
    )
  }
}
