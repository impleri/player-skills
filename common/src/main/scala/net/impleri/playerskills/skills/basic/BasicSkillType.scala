package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.chaining._

case class BasicSkillType() extends SkillType[Boolean] {
  override def name: ResourceLocation = BasicSkillType.NAME

  override def castToString(value: Option[Boolean]): Option[String] =
    value.map(if (_) BasicSkillType.STRING_TRUE else BasicSkillType.STRING_FALSE)

  override def castFromString(value: Option[String]): Option[Boolean] =
    value.map(_ == BasicSkillType.STRING_TRUE)

  override def can(skill: Skill[Boolean], threshold: Option[Boolean]): Boolean =
    (threshold.getOrElse(true) == skill.value.getOrElse(false))
      .tap(c => PlayerSkillsLogger.SKILLS.debug(
        s"Checking if player can ${skill.name} (does $threshold == ${skill.value}? $c)",
      ))

  override def getPrevValue(skill: Skill[Boolean], min: Option[Boolean], max: Option[Boolean]): Option[Boolean] = Some(false)

  override def getNextValue(skill: Skill[Boolean], min: Option[Boolean], max: Option[Boolean]): Option[Boolean] = Some(true)
}

object BasicSkillType {
  val NAME: ResourceLocation = SkillResourceLocation.of("basic").get

  private val STRING_TRUE = "true"
  private val STRING_FALSE = "true"
}
