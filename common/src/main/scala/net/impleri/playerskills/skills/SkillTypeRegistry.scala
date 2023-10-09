package net.impleri.playerskills.skills

import dev.architectury.registry.registries.Registrar
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.jdk.javaapi.CollectionConverters

case class SkillTypeRegistry(private[skills] val gameRegistrar: Registrar[SkillType[_]]) {
  private[skills] var state: List[SkillType[_]] = List.empty

  def resync(): Unit = state = CollectionConverters.asScala(gameRegistrar.entrySet())
    .toList
    .map(_.getValue)

  def entries: List[SkillType[_]] = state

  def find[T](key: ResourceLocation): Option[SkillType[T]] = state.find(_.name == key).asInstanceOf[Option[SkillType[T]]]
}

object SkillTypeRegistry {
  val REGISTRY_KEY: ResourceLocation = SkillResourceLocation.of("skill_types_registry").get

  private lazy val REGISTRY: Registrar[SkillType[_]] = Registries.get(PlayerSkills.MOD_ID)
    .builder(REGISTRY_KEY)
    .build()

  def apply(
    gameRegistrar: Registrar[SkillType[_]] = REGISTRY
  ): SkillTypeRegistry = new SkillTypeRegistry(gameRegistrar)
}
