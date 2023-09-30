package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.minecraft.resources.ResourceLocation

case class BasicSkill(
  override val name: ResourceLocation,
  override val value: Option[Boolean],
  override val description: Option[String],
  override val options: List[Boolean],
  override val changesAllowed: Int,
  override val teamMode: TeamMode,
  override val announceChange: Boolean,
  override val notifyKey: Option[String],
) extends Skill[Boolean] with ChangeableSkillOps[Boolean, BasicSkill] {
  override val skillType: ResourceLocation = BasicSkillType.NAME

  override protected[playerskills] def mutate(value: Option[Boolean], changesAllowed: Int): BasicSkill =
    copy(value = value, changesAllowed = changesAllowed)

  override def getMessageKey: String =
    value match {
      case Some(true) => "playerskills.notify.basic_skill_enabled"
      case _ => "playerskills.notify.basic_skill_disabled"
    }
}
