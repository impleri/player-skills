package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.minecraft.resources.ResourceLocation

case class NumericSkill(
  override val name: ResourceLocation,
  override val value: Option[Double] = None,
  override val description: Option[String] = None,
  override val options: List[Double] = List.empty,
  override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
  override val teamMode: TeamMode = TeamMode.Off(),
  override val announceChange: Boolean = false,
  override val notifyKey: Option[String] = None,
  step: Double = NumericSkill.DefaultStep,
) extends Skill[Double] with ChangeableSkillOps[Double, NumericSkill] {
  override val skillType: ResourceLocation = NumericSkillType.NAME

  override protected[playerskills] def mutate(value: Option[Double], changesAllowed: Int): NumericSkill = {
    copy(value = value, changesAllowed = changesAllowed)
  }
}

object NumericSkill {
  val DefaultStep: Double = 1.0
}
