package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.MinMaxCalculator
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.util.chaining.scalaUtilChainingOps
import scala.util.Try

case class TieredSkillType(
  override val skillOps: SkillOps = Skill(),
  private val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) extends SkillType[String] {
  override val name: ResourceLocation = TieredSkillType.NAME

  override def castToString(value: String): Option[String] = Option(value)

  override def castFromString(value: String): Option[String] = Option(value)

  private def index(skill: Skill[String])
    (target: Option[String] = skill.value, fallback: Int = TieredSkillType.SKILL_NOT_FOUND): Int = {
    target
      .map(skill.options.indexOf(_))
      .filter(_ >= 0)
      .getOrElse(fallback)
  }

  override def can(skill: Skill[String], threshold: Option[String] = None): Boolean = {
    (index(skill)() >= index(skill)(threshold, 0))
      .tap(logger.debugP(c =>
        s"Checking if player can ${skill.name} (is $threshold above ${skill.value}? $c)",
      ),
      )
  }

  private def getMinValue(skill: Skill[String], min: Option[String]) = index(skill)(min, 0)

  private def getMaxValue(skill: Skill[String], max: Option[String]) = index(skill)(max, skill.options.size)

  private def getCurrentValue(skill: Skill[String]) = index(skill)()

  private def getValue(options: Seq[String])(index: Double): Option[String] = Try(options.apply(index.toInt)).toOption

  override def getPrevValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = {
    getCurrentValue(skill)
      .pipe(v => MinMaxCalculator.calculate(Option(v), Option(getMaxValue(skill, max)), MinMaxCalculator.isLessThan))
      .map(_ - 1)
      .pipe(MinMaxCalculator.calculate(_, Option(getMinValue(skill, min)), MinMaxCalculator.isGreaterThan))
      .flatMap(getValue(skill.options))
  }

  override def getNextValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = {
    getCurrentValue(skill)
      .pipe(v => MinMaxCalculator.calculate(Option(v), Option(getMinValue(skill, min)), MinMaxCalculator.isGreaterThan))
      .map(_ + 1)
      .pipe(MinMaxCalculator.calculate(_, Option(getMaxValue(skill, max)), MinMaxCalculator.isLessThan))
      .flatMap(getValue(skill.options))
  }
}

object TieredSkillType {
  val NAME: ResourceLocation = ResourceLocation("tiered").get

  val SKILL_NOT_FOUND: Int = -1
}
