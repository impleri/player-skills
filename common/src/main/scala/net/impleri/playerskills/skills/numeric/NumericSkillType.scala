package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.MinMaxCalculator
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.util.chaining.scalaUtilChainingOps

case class NumericSkillType(
  override val skillOps: SkillOps = Skill(),
  private val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) extends SkillType[Double] {
  override val name: ResourceLocation = NumericSkillType.NAME

  override def castToString(value: Double): Option[String] = {
    Option(value.toString)
  }

  override def castFromString(value: String): Option[Double] = {
    value.toDoubleOption
  }

  override def can(skill: Skill[Double], threshold: Option[Double]): Boolean = {
    (skill.value.getOrElse(0.0) >= threshold.getOrElse(skill.asInstanceOf[NumericSkill].step))
      .tap(
        logger.debugP(c =>
          s"Checking if player can ${skill.name} (is $threshold >= ${skill.value}? $c)",
        ),
      )
  }

  override def getPrevValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] = {
    MinMaxCalculator.calculate(skill.value, max, MinMaxCalculator.isLessThan)
      .map(_ - skill.asInstanceOf[NumericSkill].step)
      .pipe(MinMaxCalculator.calculate(_, min, MinMaxCalculator.isGreaterThan))
  }

  override def getNextValue(skill: Skill[Double], min: Option[Double], max: Option[Double]): Option[Double] = {
    MinMaxCalculator.calculate(skill.value, min, MinMaxCalculator.isGreaterThan)
      .map(_ + skill.asInstanceOf[NumericSkill].step)
      .pipe(MinMaxCalculator.calculate(_, max, MinMaxCalculator.isLessThan))
  }
}

object NumericSkillType {
  val NAME: ResourceLocation = ResourceLocation("numeric").get
}
