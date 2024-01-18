package net.impleri.playerskills.skills

import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.architectury.Registrar
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

case class SkillTypeRegistry(private[skills] val gameRegistrar: Registrar[SkillType[_]]) {
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
  val REGISTRY_KEY: ResourceLocation = ResourceLocation("skill_types_registry").get

  lazy val REGISTRAR: Registrar[SkillType[_]] = Registrar(REGISTRY_KEY.name)

  def apply(
    gameRegistrar: Registrar[SkillType[_]] = Registrar(None),
  ): SkillTypeRegistry = {
    new SkillTypeRegistry(gameRegistrar)
  }
}
