package net.impleri.playerskills.api.skills

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

trait Skill[T] extends SkillData[T] with ChangeableSkill[T] with TranslatableSkill[T]

/**
 * Facade to Skills registry for interacting with the registered skills
 */
object Skill {
  val REGISTRY_KEY: ResourceLocation = SkillRegistry.REGISTRY_KEY

  def all(): List[Skill[_]] = StateContainer.SKILLS.entries

  def get[T](name: ResourceLocation): Option[Skill[T]] = StateContainer.SKILLS.find(name).asInstanceOf[Option[Skill[T]]]

  def upsert[T](skill: Skill[T]): Unit =
    skill
      .tap(PlayerSkillsLogger.SKILLS.infoP(s => s"Saving skill ${s.name}"))
      .pipe(StateContainer.SKILLS.upsert)

  def remove(skill: Skill[_]): Unit = StateContainer.SKILLS.remove(skill)

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
