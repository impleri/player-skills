package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class NumericSkillType(
  override val skillOps: SkillOps = Skill(),
  private val logger: Logger = PlayerSkillsLogger.SKILLS,
) extends SkillType[Double] {
  override val name: ResourceLocation = NumericSkillType.NAME

  override def castToString(value: Double): Option[String] =
    Option(value.toString)

  override def castFromString(value: String): Option[Double] =
    value.toDoubleOption

  override def can(skill: Skill[Double], threshold: Option[Double]): Boolean =
    (skill.value.getOrElse(0.0) >= threshold.getOrElse(
      skill.asInstanceOf[NumericSkill].step,
    ))
      .tap(
        logger.debugP(c =>
          s"Checking if player can ${skill.name} (is $threshold >= ${skill.value}? $c)",
        ),
      )

  private def stepFor(skill: Skill[Double]): Double = skill.asInstanceOf[NumericSkill].step

  private def floor(value: Double, min: Option[Double]): Double = min.fold(value)(value max _)

  private def ceil(value: Double, max: Option[Double]): Double = max.fold(value)(value min _)

  override def getPrevValue(
    skill: Skill[Double],
    min: Option[Double],
    max: Option[Double],
  ): Option[Double] =
    for {
      next <- skill.value.map(_ - stepFor(skill))
      floored = floor(next, min)
      ceiling = ceil(floored, max)
    } yield ceiling

  override def getNextValue(
    skill: Skill[Double],
    min: Option[Double],
    max: Option[Double],
  ): Option[Double] =
  for {
    next <- skill.value.map(_ + stepFor(skill))
    floored = floor(next, min)
    ceiling = ceil(floored, max)
  } yield ceiling
}

object NumericSkillType {
  val NAME: ResourceLocation = ResourceLocation("numeric").get
}
