package net.impleri.playerskills.skills.registry

import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.jdk.javaapi.CollectionConverters

object SkillTypes {
  val REGISTRY_KEY: ResourceLocation = SkillResourceLocation.of("skill_types_registry").get

  private val REGISTRY = Registries.get(PlayerSkills.MOD_ID)
    .builder(REGISTRY_KEY)
    .build()

  private[playerskills] def init(): Unit = ()

  def entries: List[SkillType[_]] = CollectionConverters.asScala(REGISTRY.entrySet())
    .toList
    .map(_.getValue)

  def find[T](key: ResourceLocation): Option[SkillType[T]] = Option(REGISTRY.get(key))
}
