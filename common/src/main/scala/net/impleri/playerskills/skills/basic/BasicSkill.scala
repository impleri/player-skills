package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

case class BasicSkill(
  override val name: ResourceLocation,
  override val value: Option[Boolean] = None,
  override val description: Option[String] = None,
  override val options: List[Boolean] = List.empty,
  override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
  override val teamMode: TeamMode = TeamMode.Off(),
  override val announceChange: Boolean = false,
  override val notifyKey: Option[String] = None,
) extends Skill[Boolean] with ChangeableSkillOps[Boolean, BasicSkill] {
  override val skillType: ResourceLocation = BasicSkillType.NAME

  override protected[playerskills] def mutate(value: Option[Boolean], changesAllowed: Int): BasicSkill = {
    copy(value = value, changesAllowed = changesAllowed)
  }

  override def getMessageKey: String = {
    value match {
      case Some(true) => "playerskills.notify.basic_skill_enabled"
      case _ => "playerskills.notify.basic_skill_disabled"
    }
  }
}
