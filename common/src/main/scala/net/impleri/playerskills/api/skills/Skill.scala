package net.impleri.playerskills.api.skills

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.ChatFormatting

sealed trait SkillData[T] {
  val name: ResourceLocation = SkillResourceLocation.of("empty").get
  val skillType: ResourceLocation = SkillResourceLocation.of("empty").get
  val value: Option[T] = None
  val description: Option[String] = None
  val teamMode: TeamMode = TeamMode.Off()
}

sealed trait ChangeableSkill[T] extends SkillData[T] {
  val options: List[T] = List()

  val changesAllowed: Int = Skill.UNLIMITED_CHANGES

  def areChangesAllowed(): Boolean = changesAllowed != 0

  def isAllowedValue(nextValue: Option[T]): Boolean = {
    options.isEmpty || nextValue
      .fold(nextValue.isEmpty)(options.contains(_))
  }
}

trait ChangeableSkillOps[T, S <: ChangeableSkill[T]] extends ChangeableSkill[T] {
  def mutate(newValue: Option[T] = None): S = mutate(newValue, changesAllowed - 1)

  protected[playerskills] def mutate(value: Option[T], changesAllowed: Int): S
}

sealed trait TranslatableSkill[T] extends SkillData[T] {
  val announceChange: Boolean = false

  def notifyKey: Option[String] = None

  protected def getMessageKey: String = "playerskills.notify.skill_change"

  protected def formatSkillName(): Component = {
    Component.literal(name.getPath.replace("_", " "))
      .withStyle(ChatFormatting.DARK_AQUA)
      .withStyle(ChatFormatting.BOLD)
  }

  protected def formatSkillValue(value: Option[T] = this.value): Component = {
    Component.literal(value.fold("")(v => s"$v"))
      .withStyle(ChatFormatting.GOLD)
  }

  protected def formatNotificationMessage(messageKey: String, oldValue: Option[T] = None): Component = {
    Component.translatable(messageKey, formatSkillName(), formatSkillValue(), formatSkillValue(oldValue))
  }

  protected def formatNotification(oldValue: Option[T] = None): Component = {
    formatNotificationMessage(notifyKey.getOrElse(getMessageKey), oldValue)
  }

  def getNotification(oldValue: Option[T] = None): Option[Component] = {
    if (!announceChange) None else value.map(_ => formatNotification(oldValue))
  }
}

trait Skill[T] extends SkillData[T] with ChangeableSkill[T] with TranslatableSkill[T]

/**
 * Facade to Skills registry for interacting with the registered skills
 */
trait SkillRegistryFacade {
  protected def state: SkillRegistry

  protected def logger: PlayerSkillsLogger

  def all(): List[Skill[_]] = state.entries

  def get[T](name: ResourceLocation): Option[Skill[T]] = state.find(name).asInstanceOf[Option[Skill[T]]]

  def upsert[T](skill: Skill[T]): Unit = {
    logger.info(s"Saving skill ${skill.name}")
    state.upsert(skill)
  }

  def remove(skill: Skill[_]): Unit = state.removeSkill(skill)
}

class SkillOps(
  private val skillType: SkillTypeOps,
  protected val state: SkillRegistry,
  protected val logger: PlayerSkillsLogger,
) extends SkillRegistryFacade {
  def calculatePrev[T](skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T] = {
    skillType.get(skill)
      .flatMap(_.getPrevValue(skill, min, max))
  }

  def calculateNext[T](skill: Skill[T], min: Option[T] = None, max: Option[T] = None): Option[T] = {
    skillType.get(skill)
      .flatMap(_.getNextValue(skill, min, max))
  }

  def sortHelper[T](x: Skill[T], y: Skill[T]): Int = {
    val skillTypeOpt = skillType.get(x)
    val xGreater = skillTypeOpt.map(_.can(x, y.value))
    val yGreater = skillTypeOpt.map(_.can(y, x.value))

    (xGreater, yGreater) match {
      case (Some(true), Some(true)) => 0
      case (Some(true), _) => -1
      case (_, Some(true)) => 1
      case _ => 0
    }
  }
}

object Skill {
  val REGISTRY_KEY: ResourceLocation = SkillRegistry.REGISTRY_KEY

  val UNLIMITED_CHANGES: Int = -1

  def apply(
    skillType: SkillTypeOps = SkillType(),
    state: SkillRegistry = StateContainer.SKILLS,
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): SkillOps = {
    new SkillOps(skillType, state, logger)
  }
}
