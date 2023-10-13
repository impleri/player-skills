package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.chaining._

case class TieredSkillType() extends SkillType[String] {
  override def name: ResourceLocation = TieredSkillType.NAME

  override def castToString(value: Option[String]): Option[String] = value

  override def castFromString(value: Option[String]): Option[String] = value

  private def index(target: Option[String], options: Seq[String], fallback: Int = 0): Int = {
    target
      .map(options.indexOf(_))
      .filter(_ >= 0)
      .getOrElse(fallback)
  }

  override def can(skill: Skill[String], threshold: Option[String]): Boolean = {
    (index(skill.value, skill.options, -1) >= index(threshold, skill.options))
      .tap(PlayerSkillsLogger.SKILLS.debugP(c =>
        s"Checking if player can ${skill.name} (is $threshold above ${skill.value}? $c)",
      ),
      )
  }

  override def getPrevValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = {
    skill.value
      .map(_ => index(skill.value, skill.options, -1))
      .map(math.max(_, index(max, skill.options, skill.options.size)))
      .map(_ - 1)
      .map(math.min(_, index(min, skill.options)))
      .map(skill.options.apply)
  }

  override def getNextValue(skill: Skill[String], min: Option[String], max: Option[String]): Option[String] = {
    skill.value
      .map(_ => index(skill.value, skill.options, -1))
      .map(math.min(_, index(min, skill.options)))
      .map(_ + 1)
      .map(math.max(_, index(max, skill.options, skill.options.size)))
      .map(skill.options.apply)
  }
}

object TieredSkillType {
  val NAME: ResourceLocation = SkillResourceLocation.of("tiered").get
}
