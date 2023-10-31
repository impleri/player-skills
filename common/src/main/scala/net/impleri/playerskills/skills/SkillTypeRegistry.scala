package net.impleri.playerskills.skills

import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.ArchitecturyRegistrar
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

case class SkillTypeRegistry(private[skills] val gameRegistrar: ArchitecturyRegistrar[SkillType[_]]) {
  private[skills] var state: List[SkillType[_]] = List.empty

  def resync(): Unit = {
    state = gameRegistrar.entries().values.toList
  }

  def entries: List[SkillType[_]] = state

  def find[T](key: ResourceLocation): Option[SkillType[T]] = {
    state
      .find(_.name == key)
      .asInstanceOf[Option[SkillType[T]]]
  }
}

object SkillTypeRegistry {
  val REGISTRY_KEY: ResourceLocation = SkillResourceLocation.of("skill_types_registry").get

  lazy val REGISTRAR: ArchitecturyRegistrar[SkillType[_]] = ArchitecturyRegistrar(REGISTRY_KEY)

  def apply(
    gameRegistrar: ArchitecturyRegistrar[SkillType[_]] = ArchitecturyRegistrar(None),
  ): SkillTypeRegistry = {
    new SkillTypeRegistry(gameRegistrar)
  }
}
