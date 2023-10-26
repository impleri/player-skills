package net.impleri.playerskills.server.api

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import java.util.UUID

/**
 * Facade to the Players registry for interacting with skills for a given player
 */
trait PlayerRegistryFacade {
  protected def registry: PlayerRegistry

  def get(playerId: UUID): List[Skill[_]] = registry.get(playerId)

  def get(player: MinePlayer): List[Skill[_]] = get(player.getUUID)

  def get[T](playerId: UUID, name: ResourceLocation): Option[Skill[T]] = {
    registry.get(playerId)
      .find(_.name == name)
      .asInstanceOf[Option[Skill[T]]]
  }

  def get[T](player: MinePlayer, name: ResourceLocation): Option[Skill[T]] = {
    get(player.getUUID, name)
  }

  def isOnline(playerId: UUID): Boolean = registry.has(playerId)

  def open(playerId: UUID): List[Skill[_]] = registry.open(playerId)

  def open(players: List[UUID]): List[UUID] = registry.open(players)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] = registry.upsert(playerId, skill)

  def upsert(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = upsert(player.getUUID, skill)

  def close(playerId: UUID): Boolean = registry.close(playerId)

  def close(players: List[UUID]): Boolean = registry.close(players).nonEmpty
}

class Player(
  override val registry: PlayerRegistry,
  protected val skillTypeOps: SkillTypeOps,
  protected val skillOps: SkillOps,
) extends PlayerRegistryFacade {
  private def canHelper[T](playerId: UUID, skill: Skill[T]): Option[(SkillType[T], Skill[T])] = {
    (skillTypeOps.get(skill), get[T](playerId, skill.name)) match {
      case (Some(t), Some(s)) => Option((t, s))
      case _ => None
    }
  }

  def can[T](playerId: UUID, skill: Skill[T], expectedValue: Option[T] = None): Boolean = {
    canHelper[T](playerId, skill).fold(Player.DEFAULT_SKILL_RESPONSE)(t => t._1.can(t._2, expectedValue))
  }

  def reset(playerId: UUID, skill: Skill[_]): List[Skill[_]] = {
    skillOps.get(skill.name)
      .asInstanceOf[Option[Skill[_]]]
      .map(upsert(playerId, _))
      .getOrElse(List.empty)
  }

  def reset(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = reset(player.getUUID, skill)

  def calculateValue[T](player: MinePlayer, skill: Skill[T], value: Option[T]): Option[Skill[T]] = {
    get[T](player, skill.name)
      .orElse(skillOps.get[T](skill.name))
      .filter(_.areChangesAllowed())
      .filter(_.isAllowedValue(value))
      .filter(_.value != value)
      .map(_.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(value))
  }
}

object Player {
  val DEFAULT_SKILL_RESPONSE: Boolean = true

  def apply(
    registry: PlayerRegistry = ServerStateContainer.PLAYERS,
    skillTypeOps: SkillTypeOps = SkillType(),
    skillOps: SkillOps = Skill(),
  ): Player = {
    new Player(registry, skillTypeOps, skillOps)
  }
}
