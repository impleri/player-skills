package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps
import scala.util.Try

case class TieredSkillType(
  override val skillOps: SkillOps = Skill(),
  private val logger: Logger = PlayerSkillsLogger.SKILLS,
) extends SkillType[String] {
  override val name: ResourceLocation = TieredSkillType.NAME

  override def castToString(value: String): Option[String] = Option(value)

  override def castFromString(value: String): Option[String] = Option(value)

  private def indexOf(
    skill: Skill[String],
    target: Option[String] = None,
  ): Option[Int] =
    target
      .map(skill.options.indexOf)
      .filter(_ >= 0)
      .filter(_ < skill.options.size)

  private def compare(
    skill: Skill[String],
    threshold: Option[String],
  ): Boolean =
    (indexOf(skill, skill.value), indexOf(skill, threshold)) match {
      case (Some(v), Some(t)) => v >= t
      case (Some(_), _)       => true
      case _                  => false
    }

  override def can(
    skill: Skill[String],
    threshold: Option[String] = None,
  ): Boolean =
    compare(skill, threshold)
      .tap(
        logger.debugP(c =>
          s"Checking if player can ${skill.name} (is $threshold above ${skill.value}? $c)",
        ),
      )

  private def floor(
    skill: Skill[String],
    value: Int,
    min: Option[String],
  ): Int = indexOf(skill, min).fold(value)(value max _)

  private def ceil(skill: Skill[String], value: Int, max: Option[String]): Int =
    indexOf(skill, max).fold(value)(value min _)

  override def getPrevValue(
    skill: Skill[String],
    min: Option[String],
    max: Option[String],
  ): Option[String] =
    for {
      next <- indexOf(skill, skill.value).map(_ - 1)
      floored = floor(skill, next, min)
      ceiling = ceil(skill, floored, max)
      nextString <- Try(skill.options.apply(ceiling)).toOption
    } yield nextString

  override def getNextValue(
    skill: Skill[String],
    min: Option[String],
    max: Option[String],
  ): Option[String] =
    for {
      next <- indexOf(skill, skill.value).map(_ + 1)
      floored = floor(skill, next, min)
      ceiling = ceil(skill, floored, max)
      nextString <- Try(skill.options.apply(ceiling)).toOption
    } yield nextString
}

object TieredSkillType {
  val NAME: ResourceLocation = ResourceLocation("tiered").get
}
