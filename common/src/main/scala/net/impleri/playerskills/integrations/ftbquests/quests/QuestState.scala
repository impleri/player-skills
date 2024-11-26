package net.impleri.playerskills.integrations.ftbquests.quests

import net.impleri.slab.resources.ResourceLocation

case class QuestState[T](
  skillType: ResourceLocation,
  downgrade: Boolean = false,
  skill: Option[ResourceLocation] = None,
  value: Option[T] = None,
  min: Option[T] = None,
  max: Option[T] = None,
) {
  def skillAsString: String = skill.fold("")(_.asString)

  def isValid: Boolean = skill.nonEmpty && value.nonEmpty
}
