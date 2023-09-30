package net.impleri.playerskills.api.skills

import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import scala.util.chaining._

trait Skill[T] extends SkillData[T] with ChangeableSkill[T] with TranslatableSkill[T]

object Skill {
  val REGISTRY_KEY: ResourceLocation = Skills.REGISTRY_KEY

  def all(): List[Skill[_]] = Skills.entries

  def get[T](name: ResourceLocation): Option[Skill[T]] = Skills.find(name).asInstanceOf[Option[Skill[T]]]

  def upsert[T](skill: Skill[T]): Boolean =
    skill
      .tap(s => PlayerSkillsLogger.SKILLS.info(s"Saving skill ${s.name}"))
      .pipe(Skills.upsert)

  def remove(skill: Skill[_]): Boolean = Skills.remove(skill)

  def calculatePrev[T](skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T] =
    SkillType.get(skill.skillType)
      .asInstanceOf[Option[SkillType[T]]]
      .flatMap(_.getPrevValue(skill, min, max))

  def calculateNext[T](skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T] =
    SkillType.get(skill.skillType)
      .asInstanceOf[Option[SkillType[T]]]
      .flatMap(_.getNextValue(skill, min, max))

  def sortHelper[T](x: Skill[T], y: Skill[T]): Int = {
    val skillType = SkillType.get(x)
    val xGreater = skillType.map(_.can(x, y.value))
    val yGreater = skillType.map(_.can(y, x.value))

    (xGreater, yGreater) match {
      case (Some(true), Some(true)) => 0
      case (Some(true), _) => -1
      case (_, Some(true)) => 1
      case _ => 0
    }
  }
}
