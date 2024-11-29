package net.impleri.playerskills.server.api

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.{Player => MinecraftPlayer}
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/** Facade to the Players registry for interacting with skills for a given
  * player
  */
trait PlayerRegistryFacade {
  protected def registry: PlayerRegistry

  def get(playerId: UUID): List[Skill[_]] = registry.get(playerId)

  def get(player: MinecraftPlayer): List[Skill[_]] = get(player.uuid)

  def get[T](playerId: UUID, name: ResourceLocation): Option[Skill[T]] =
    registry
      .get(playerId)
      .find(_.name == name)
      .asInstanceOf[Option[Skill[T]]]

  def get[T](
    player: MinecraftPlayer,
    name: ResourceLocation,
  ): Option[Skill[T]] =
    get(player.uuid, name)

  def isOnline(playerId: UUID): Boolean = registry.has(playerId)

  def open(playerId: UUID): List[Skill[_]] = registry.open(playerId)

  def open(players: List[UUID]): List[UUID] = registry.open(players)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    registry.upsert(playerId, skill)

  def upsert(player: MinecraftPlayer, skill: Skill[_]): List[Skill[_]] =
    upsert(player.uuid, skill)

  def close(playerId: UUID): Boolean = registry.close(playerId)

  def close(players: List[UUID]): Boolean = registry.close(players).nonEmpty
}

class Player(
  getRegistry: => PlayerRegistry,
  protected val skillTypeOps: SkillTypeOps,
  protected val skillOps: SkillOps,
  private val logger: Logger,
) extends PlayerRegistryFacade {
  override lazy val registry: PlayerRegistry = getRegistry

  private def canHelper[T](
    playerId: UUID,
    skillName: ResourceLocation,
    expectedValue: Option[T] = None,
  ): Option[Boolean] =
    for {
      skill <- get[T](playerId, skillName)
      skillType <- skillTypeOps.get[T](skill)
    } yield skillType.can(skill, expectedValue)
      .tap(logger.debugP(c => s"Checked that $playerId can ${skill.name} as $expectedValue: $c"))

  def can[T](
    playerId: UUID,
    skillName: ResourceLocation,
    expectedValue: Option[T] = None,
  ): Boolean =
    canHelper(playerId, skillName, expectedValue)
      .getOrElse {
        logger.warn(s"Could not find a valid skill for $skillName or an associated type")
        Player.DEFAULT_SKILL_RESPONSE
      }


  def reset(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    skillOps
      .get(skill.name)
      .asInstanceOf[Option[Skill[_]]]
      .map(upsert(playerId, _))
      .getOrElse(List.empty)

  def reset(player: MinecraftPlayer, skill: Skill[_]): List[Skill[_]] =
    reset(player.uuid, skill)

  def calculateValue[T](
    player: UUID,
    skill: Skill[T],
    value: Option[T],
  ): Option[Skill[T]] =
    get[T](player, skill.name)
      .orElse(skillOps.get[T](skill.name))
      .filter(_.areChangesAllowed())
      .filter(_.isAllowedValue(value))
      .filter(_.value != value)
      .map(_.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(value))

  def calculateValue[T](
    player: MinecraftPlayer,
    skill: Skill[T],
    value: Option[T],
  ): Option[Skill[T]] =
    calculateValue(player.uuid, skill, value)
}

object Player {
  /**
   * Default Skill Response
   *
   * This is used primarily when we are missing a SkillType or a Skill for the Player. We err on the side of caution here.
   */
  val DEFAULT_SKILL_RESPONSE: Boolean = true

  def apply(
    registry: PlayerRegistry = PlayerRegistry(),
    skillTypeOps: SkillTypeOps = SkillType(),
    skillOps: SkillOps = Skill(),
    logger: Logger = PlayerSkillsLogger.RESTRICTIONS,
  ): Player =
    new Player(registry, skillTypeOps, skillOps, logger)
}
