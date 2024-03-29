package net.impleri.playerskills.api.skills

import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

sealed trait ChangeableSkillType[T] {
  def getPrevValue(skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T]

  def getNextValue(skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T]
}

sealed trait SerializableSkillType[T] {
  protected def skillOps: SkillOps

  protected def castToString(value: T): Option[String]

  def castFromString(value: String): Option[T]

  def serialize(skill: Skill[T]): String = {
    List(
      s"${skill.name}",
      s"${skill.skillType}",
      skill.value.flatMap(castToString).getOrElse(SkillType.stringValueNone),
      s"${skill.changesAllowed}",
    ).mkString(SkillType.stringValueSeparator)
  }

  def deserialize(name: String, value: Option[String], changesAllowed: Int): Option[Skill[T]] = {
    SkillResourceLocation(name)
      .flatMap(skillOps.get[T])
      .map(_.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(value.flatMap(castFromString), changesAllowed))
  }
}

trait SkillType[T] extends ChangeableSkillType[T] with SerializableSkillType[T] {
  val name: ResourceLocation = SkillResourceLocation("skill").get

  def can(skill: Skill[T], threshold: Option[T] = None): Boolean = {
    skill.value.exists(v => threshold.forall(v == _))
  }
}

/**
 * Facade to Skill Types registry for interacting with registered skill types
 */
sealed trait SkillTypeRegistryFacade {
  protected def state: SkillTypeRegistry

  def all(): List[SkillType[_]] = state.entries

  def get[T](name: ResourceLocation): Option[SkillType[T]] = state.find(name)

  def get[T](name: String): Option[SkillType[T]] = SkillResourceLocation.of(name).flatMap(state.find)

  def get[T](skill: Skill[T]): Option[SkillType[T]] = get(skill.skillType)
}

class SkillTypeOps(
  override val state: SkillTypeRegistry,
  protected val logger: PlayerSkillsLogger,
) extends SkillTypeRegistryFacade {
  def serialize[T](skill: Skill[T]): Option[String] = {
    get(skill)
      .map(_.serialize(skill))
      .tap(logger.debugP(v => s"Dehydrated skill ${skill.name} of type ${skill.skillType} for storage: $v"))
  }

  private def splitRawSkill(value: String) = {
    value.split(SkillType.stringValueSeparator)
      .toList
      .reverse
      .dropWhile(_.isEmpty)
      .reverse
  }

  private def parseValue(value: String): Option[String] = {
    if (value == SkillType.stringValueNone) None else Option(value)
  }

  private def parseChanges(value: String): Int = {
    Try(value.toInt)
      .toOption
      .getOrElse {
        logger.warn(s"Unable to parse changesAllowed ($value) back into an integer, data possibly corrupted")
        0
      }
  }

  private def createSkill[T](parts: List[String]): Option[Skill[T]] = {
    parts match {
      case name :: skillType :: value :: changesAllowed :: _ => {
        logger.debug(s"Hydrating $skillType skill named $name: $value")
        SkillResourceLocation(skillType)
          .flatMap(get[T])
          .flatMap(_.deserialize(name, parseValue(value), parseChanges(changesAllowed)))
      }
      case _ => {
        logger.error(s"Tried to parse skill with incorrectly stored data: ${parts.mkString("|||")}")
        None
      }
    }
  }

  def deserialize(value: String): Option[Skill[_]] = {
    splitRawSkill(value)
      .pipe(createSkill)
  }

  def deserializeAll(values: List[String]): List[Skill[_]] = {
    values
      .partition(_.isEmpty)
      .tap(_._1.foreach(logger.warnP(v => s"Unable to unpack skill $v from storage")))
      ._2
      .flatMap(deserialize)
  }
}

/**
 * Facade to Skill Types registry for interacting with registered skill types
 */
object SkillType {
  val REGISTRY_KEY: ResourceLocation = SkillTypeRegistry.REGISTRY_KEY

  private[skills] val stringValueNone: String = "[NULL]"
  private[skills] val stringValueSeparator: String = ";"

  def apply(
    state: SkillTypeRegistry = SkillTypeRegistry(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): SkillTypeOps = {
    new SkillTypeOps(state, logger)
  }
}
