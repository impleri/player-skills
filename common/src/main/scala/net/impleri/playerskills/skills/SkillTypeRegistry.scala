package net.impleri.playerskills.skills

import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.PlayerSkills
import net.impleri.slab.registry.Registrar
import net.impleri.slab.resources.ResourceLocation

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

  lazy val REGISTRAR: Registrar[SkillType[_]] = PlayerSkills.REGISTRAR_FACTORY.create(REGISTRY_KEY)

  def apply(
    gameRegistrar: Registrar[SkillType[_]] = Registrar(None),
  ): SkillTypeRegistry = {
    new SkillTypeRegistry(gameRegistrar)
  }
}
