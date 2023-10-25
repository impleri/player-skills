package net.impleri.playerskills.skills.specialized

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.minecraft.resources.ResourceLocation

case class SpecializedSkill(
  override val name: ResourceLocation,
  override val value: Option[String] = None,
  override val description: Option[String] = None,
  override val options: List[String] = List.empty,
  override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
  override val teamMode: TeamMode = TeamMode.Off(),
  override val announceChange: Boolean = false,
  override val notifyKey: Option[String] = None,
) extends Skill[String] with ChangeableSkillOps[String, SpecializedSkill] {
  override val skillType: ResourceLocation = SpecializedSkillType.NAME

  override protected[playerskills] def mutate(value: Option[String], changesAllowed: Int): SpecializedSkill = {
    copy(
      value = value, changesAllowed = changesAllowed,
    )
  }

  override def getMessageKey: String = "playerskills.notify.specialized_skill_selected"
}
