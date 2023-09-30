package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.minecraft.resources.ResourceLocation

case class TieredSkill(
  override val name: ResourceLocation,
  override val value: Option[String],
  override val description: Option[String],
  override val options: List[String],
  override val changesAllowed: Int,
  override val teamMode: TeamMode,
  override val announceChange: Boolean,
  override val notifyKey: Option[String],
) extends Skill[String] with ChangeableSkillOps[String, TieredSkill] {
  override val skillType: ResourceLocation = TieredSkillType.NAME

  override protected[playerskills] def mutate(value: Option[String], changesAllowed: Int): TieredSkill = copy(
    value = value, changesAllowed = changesAllowed)
}
