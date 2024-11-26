package net.impleri.playerskills.api.skills

import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.{Named, ResourceLocation}

sealed trait SkillData[T] extends Named {
  val name: ResourceLocation = ResourceLocation("empty").get
  val skillType: ResourceLocation = ResourceLocation("empty").get
  val value: Option[T] = None
  val description: Option[String] = None
  val teamMode: TeamMode = TeamMode.Off()
}

sealed trait ChangeableSkill[T] extends SkillData[T] {
  val options: List[T] = List()

  val changesAllowed: Int = Skill.UNLIMITED_CHANGES

  def areChangesAllowed(): Boolean = changesAllowed != 0

  def isAllowedValue(nextValue: Option[T]): Boolean =
    options.isEmpty ||
      nextValue.fold(nextValue.isEmpty)(options.contains(_))
}

// This is separated from ChangeableSkill above in order to get mutate's return type to be the resolved skill rather
// than the generic Skill[T]. Each new skill class should inherit ChangeableSkillOps in addition to Skill.
trait ChangeableSkillOps[T, S <: ChangeableSkill[T]]
    extends ChangeableSkill[T] {
  def mutate(newValue: Option[T] = None): S =
    mutate(newValue, changesAllowed - 1)

  protected[playerskills] def mutate(value: Option[T], changesAllowed: Int): S
}

sealed trait TranslatableSkill[T] extends SkillData[T] {
  val announceChange: Boolean = false

  def notifyKey: Option[String] = None

  protected[playerskills] def getMessageKey: String =
    TranslatableSkill.DEFAULT_NOTIFICATION_MESSAGE

  private def formatSkillName(): StaticText =
    StaticText(name.path.replace("_", " "))
      .darkAqua()
      .bold()

  private def formatSkillValue(value: Option[T] = this.value): StaticText =
    StaticText(value.fold("")(v => s"$v"))
      .gold()

  private def formatNotificationMessage(
    messageKey: String,
    oldValue: Option[T] = None,
  ): TranslatableText =
    TranslatableText(
      messageKey,
      formatSkillName(),
      formatSkillValue(),
      formatSkillValue(oldValue),
    )

  private def formatNotification(
    oldValue: Option[T] = None,
  ): TranslatableText =
    formatNotificationMessage(notifyKey.getOrElse(getMessageKey), oldValue)

  def getNotification(oldValue: Option[T] = None): Option[TranslatableText] =
    value
      .filter(_ => announceChange)
      .map(_ => formatNotification(oldValue))
}

object TranslatableSkill {
  final val DEFAULT_NOTIFICATION_MESSAGE: String = "playerskills.notify.skill_change"
}

trait Skill[T]
    extends SkillData[T]
    with ChangeableSkill[T]
    with TranslatableSkill[T]

/** Facade to Skills registry for interacting with the registered skills
  */
trait SkillRegistryFacade {
  protected def state: SkillRegistry

  protected def logger: Logger

  def all(): List[Skill[_]] = state.entries

  def get[T](name: ResourceLocation): Option[Skill[T]] =
    state
      .find(name)
      .asInstanceOf[Option[Skill[T]]]

  def upsert[T](skill: Skill[T]): Unit = {
    logger.info(s"Saving skill ${skill.name}")
    state.upsert(skill)
  }

  def remove(skill: Skill[_]): Unit = state.removeSkill(skill)
}

class SkillOps(
  private val skillType: SkillTypeOps,
  protected val state: SkillRegistry,
  protected val logger: Logger,
) extends SkillRegistryFacade {
  def calculatePrev[T](
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[T] =
    skillType
      .get(skill)
      .flatMap(_.getPrevValue(skill, min, max))

  def calculateNext[T](
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[T] =
    skillType
      .get(skill)
      .flatMap(_.getNextValue(skill, min, max))

  private def calculateSort[T](x: Skill[T], y: Skill[T]): Option[Int] =
    for {
      skillType <- skillType.get(x)
      xGreater = skillType.can(x, y.value)
      yGreater = skillType.can(y, x.value)
    } yield (xGreater, yGreater) match {
      case (true, true) => 0
      case (true, _) => -1
      case (_, true) => 1
      case _ => 0
    }

  def sortHelper[T](x: Skill[T], y: Skill[T]): Int =
    calculateSort(x, y).getOrElse(0)
}

object Skill {
  val REGISTRY_KEY: ResourceLocation = SkillRegistry.REGISTRY_KEY

  val UNLIMITED_CHANGES: Int = -1

  def apply(
    skillType: SkillTypeOps = SkillType(),
    state: SkillRegistry = SkillRegistry(),
    logger: Logger = PlayerSkillsLogger.SKILLS,
  ): SkillOps =
    new SkillOps(skillType, state, logger)
}
