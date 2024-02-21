package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

case class TieredSkill(
  override val name: ResourceLocation,
  override val value: Option[String] = None,
  override val description: Option[String] = None,
  override val options: List[String] = List.empty,
  override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
  override val teamMode: TeamMode = TeamMode.Off(),
  override val announceChange: Boolean = false,
  override val notifyKey: Option[String] = None,
) extends Skill[String] with ChangeableSkillOps[String, TieredSkill] {
  override val skillType: ResourceLocation = TieredSkillType.NAME

  override protected[playerskills] def mutate(value: Option[String], changesAllowed: Int): TieredSkill = {
    copy(
      value = value, changesAllowed = changesAllowed,
    )
  }
}
