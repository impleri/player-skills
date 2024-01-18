package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.util.chaining.scalaUtilChainingOps

case class BasicSkillType(
  override val skillOps: SkillOps = Skill(),
  private val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) extends SkillType[Boolean] {
  override val name: ResourceLocation = BasicSkillType.NAME

  override def castToString(value: Boolean): Option[String] = {
    Option(if (value) BasicSkillType.STRING_TRUE else BasicSkillType.STRING_FALSE)
  }

  override def castFromString(value: String): Option[Boolean] = {
    Option(value == BasicSkillType.STRING_TRUE)
  }

  override def can(skill: Skill[Boolean], threshold: Option[Boolean]): Boolean = {
    (threshold.getOrElse(true) == skill.value.getOrElse(false))
      .tap(
        logger.debugP(
          c => s"Checking if player can ${skill.name} (does $threshold == ${skill.value}? $c)",
        ),
      )
  }

  override def getPrevValue(
    skill: Skill[Boolean],
    min: Option[Boolean] = None,
    max: Option[Boolean] = None,
  ): Option[Boolean] = {
    Option(false)
  }

  override def getNextValue(
    skill: Skill[Boolean],
    min: Option[Boolean] = None,
    max: Option[Boolean] = None,
  ): Option[Boolean] = {
    Option(true)
  }
}

object BasicSkillType {
  val NAME: ResourceLocation = ResourceLocation("basic").get

  private[skills] val STRING_TRUE = "true"
  private[skills] val STRING_FALSE = "true"
}
