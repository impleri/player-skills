package net.impleri.playerskills.api.skills

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait SkillType[T] {
  def name: ResourceLocation = SkillResourceLocation.of("skill").get

  protected def castToString(value: Option[T]): Option[String]

  def castFromString(value: Option[String]): Option[T]

  def getPrevValue(skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T]

  def getNextValue(skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T]

  def can(skill: Skill[T], threshold: Option[T] = None): Boolean =
    skill.value.exists(v => threshold.forall(v == _))

  def serialize(skill: Skill[T]): String = List(
    s"${skill.name}",
    s"${skill.skillType}",
    castToString(skill.value).getOrElse(SkillType.stringValueNone),
    s"${skill.changesAllowed}"
  ).mkString(SkillType.stringValueSeparator)

  def deserialize(name: String, value: Option[String], changesAllowed: Int): Option[Skill[T]] =
    SkillResourceLocation.of(name)
      .flatMap(Skill.get)
      .asInstanceOf[Option[Skill[T] with ChangeableSkillOps[T, Skill[T]]]]
      .map(_.mutate(castFromString(value), changesAllowed))
}

/**
 * Facade to Skill Types registry for interacting with registered skill types
 */
sealed trait SkillRegistryFacade {
  def all(): List[SkillType[_]] = StateContainer.SKILL_TYPES.entries

  def get[T](name: ResourceLocation): Option[SkillType[T]] = StateContainer.SKILL_TYPES.find(name)

  def get[T](name: String): Option[SkillType[T]] = SkillResourceLocation.of(name).flatMap(StateContainer.SKILL_TYPES.find)

  def get[T](skill: Skill[T]): Option[SkillType[T]] = get(skill.skillType)
}

sealed trait SkillTypeOps extends SkillRegistryFacade {
  def serialize[T](skill: Skill[T]): Option[String] =
    get(skill)
      .map(_.serialize(skill))
      .tap(PlayerSkillsLogger.SKILLS.debugP(v => s"Dehydrated skill ${skill.name} of type ${skill.skillType} for storage: $v"))

  private def splitRawSkill(value: String) =
    value.split(SkillType.stringValueSeparator)
      .toList
      .reverse
      .dropWhile(_.isEmpty)
      .reverse

  private def parseValue(value: String): Option[String] =
    if (value == SkillType.stringValueNone) None else Some(value)

  private def parseChanges(value: String): Int =
    Try(value.toInt)
      .toOption
      .getOrElse {
        PlayerSkillsLogger.SKILLS.error(s"Unable to parse changesAllowed ($value) back into an integer, data possibly corrupted")
        0
      }

  private def createSkill[T](parts: List[String]): Option[Skill[T]] = parts match {
    case name :: skillType :: value :: changesAllowed :: _ =>
      PlayerSkillsLogger.SKILLS.debug(s"Hydrating $skillType skill named $name: $value")
      SkillResourceLocation.of(skillType)
        .flatMap(get)
        .asInstanceOf[Option[SkillType[T]]] // Unfortunately we need to cast it because it comes back as Any
        .flatMap(_.deserialize(name, parseValue(value), parseChanges(changesAllowed)))
    case _ =>
      PlayerSkillsLogger.SKILLS.error(s"Tried to parse skill with incorrectly stored data: ${parts.mkString("|||")}")
      None
  }

  def deserialize(value: String): Option[Skill[_]] =
    value.pipe(splitRawSkill)
      .pipe(createSkill)

  def deserializeAll(values: List[String]): List[Skill[_]] =
    values
      .partition(_.isEmpty)
      .tap(_._1.foreach(PlayerSkillsLogger.SKILLS.warnP(v => s"Unable to unpack skill $v from storage")))
      ._2
      .flatMap(deserialize)
}

/**
 * Facade to Skill Types registry for interacting with registered skill types
 */
object SkillType extends SkillRegistryFacade with SkillTypeOps {
  val REGISTRY_KEY: ResourceLocation = SkillTypeRegistry.REGISTRY_KEY

  private[skills] val stringValueNone: String = "[NULL]"
  private[skills] val stringValueSeparator: String = ";"
}
