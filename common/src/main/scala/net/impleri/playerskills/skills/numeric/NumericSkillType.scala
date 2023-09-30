package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.chaining._

case class NumericSkillType() extends SkillType[Double] {
  override def name: ResourceLocation = NumericSkillType.NAME

  override def castToString(value: Option[Double]): Option[String] =
    value.map(_.toString)

  override def castFromString(value: Option[String]): Option[Double] =
    value.flatMap(_.toDoubleOption)

  override def can(skill: Skill[Double], threshold: Option[Double]): Boolean =
    (skill.value.getOrElse(0.0) >= threshold.getOrElse(skill.asInstanceOf[NumericSkill].step))
      .tap(c => PlayerSkillsLogger.SKILLS.debug(
      s"Checking if player can ${skill.name} (is $threshold >= ${skill.value}? $c)",
    ))

  override def getPrevValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] =
    skill.value
      .map(math.max(_, max.getOrElse(0.0)))
      .map(_ - skill.asInstanceOf[NumericSkill].step)
      .map(math.min(_, min.getOrElse(0.0)))

  override def getNextValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] =
    skill.value
      .map(math.min(_, min.getOrElse(0.0)))
      .map(_ + skill.asInstanceOf[NumericSkill].step)
      .map(math.max(_, max.getOrElse(0.0)))
}

object NumericSkillType {
  val NAME: ResourceLocation = SkillResourceLocation.of("numeric").get
}
