package net.impleri.playerskills.skills.specialized

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

case class SpecializedSkillType(override val skillOps: SkillOps = Skill()) extends SkillType[String] {
  override val name: ResourceLocation = SpecializedSkillType.NAME

  override def castToString(value: String): Option[String] = Option(value)

  override def castFromString(value: String): Option[String] = Option(value)

  override def can(skill: Skill[String], threshold: Option[String]): Boolean = {
    (skill.value, threshold) match {
      case (Some(v), Some(t)) => v == t
      case (Some(_), None) => true
      case _ => false
    }
  }

  override def getPrevValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = None

  override def getNextValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = None
}

object SpecializedSkillType {
  val NAME: ResourceLocation = SkillResourceLocation.of("specialized").get
}
