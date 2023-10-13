package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.minecraft.resources.ResourceLocation

case class NumericSkill(
  override val name: ResourceLocation,
  override val value: Option[Double],
  override val description: Option[String],
  override val options: List[Double],
  override val changesAllowed: Int,
  override val teamMode: TeamMode,
  override val announceChange: Boolean,
  override val notifyKey: Option[String],
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
