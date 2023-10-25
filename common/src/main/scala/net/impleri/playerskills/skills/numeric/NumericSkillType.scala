package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class NumericSkillType(override val skillOps: SkillOps = Skill()) extends SkillType[Double] {
  override def name: ResourceLocation = NumericSkillType.NAME

  override def castToString(value: Double): Option[String] = {
    Option(value.toString)
  }

  override def castFromString(value: String): Option[Double] = {
    value.toDoubleOption
  }

  override def can(skill: Skill[Double], threshold: Option[Double]): Boolean = {
    (skill.value.getOrElse(0.0) >= threshold.getOrElse(skill.asInstanceOf[NumericSkill].step))
      .tap(PlayerSkillsLogger.SKILLS.debugP(c =>
        s"Checking if player can ${skill.name} (is $threshold >= ${skill.value}? $c)",
      ),
      )
  }

  override def getPrevValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] = {
    skill.value
      .map(math.max(_, max.getOrElse(0.0)))
      .map(_ - skill.asInstanceOf[NumericSkill].step)
      .map(math.min(_, min.getOrElse(0.0)))
  }

  override def getNextValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] = {
    skill.value
      .map(math.min(_, min.getOrElse(0.0)))
      .map(_ + skill.asInstanceOf[NumericSkill].step)
      .map(math.max(_, max.getOrElse(0.0)))
  }
}

object NumericSkillType {
  val NAME: ResourceLocation = SkillResourceLocation.of("numeric").get
}
