package net.impleri.playerskills.server.api

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.server.ServerStateContainer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import java.util.UUID

/**
 * Facade to the Players registry for interacting with skills for a given player
 */
object Player {
  val DEFAULT_SKILL_RESPONSE: Boolean = true

  def get(playerId: UUID): List[Skill[_]] = ServerStateContainer.PLAYERS.get(playerId)

  def get(player: MinePlayer): List[Skill[_]] = get(player.getUUID)

  def get[T](playerId: UUID, name: ResourceLocation): Option[Skill[T]] = {
    ServerStateContainer.PLAYERS.get(playerId)
      .find(_.name == name)
      .asInstanceOf[Option[Skill[T]]]
  }

  def get[T](player: MinePlayer, name: ResourceLocation): Option[Skill[T]] = {
    get(player.getUUID, name)
  }

  def isOnline(playerId: UUID): Boolean = ServerStateContainer.PLAYERS.has(playerId)

  def open(playerId: UUID): List[Skill[_]] = ServerStateContainer.PLAYERS.open(playerId)

  def open(players: List[UUID]): List[UUID] = ServerStateContainer.PLAYERS.open(players)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] = ServerStateContainer.PLAYERS.upsert(playerId, skill)

  def upsert(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = upsert(player.getUUID, skill)

  def close(playerId: UUID): Boolean = ServerStateContainer.PLAYERS.close(playerId)

  def close(players: List[UUID]): Boolean = ServerStateContainer.PLAYERS.close(players).nonEmpty

  private def canHelper[T](playerId: UUID, skill: Skill[T]): Option[(SkillType[T], Skill[T])] = {
    (SkillType().get(skill), get[T](playerId, skill.name)) match {
      case (Some(t), Some(s)) => Option((t, s))
      case _ => None
    }
  }

  def can[T](playerId: UUID, skill: Skill[T], expectedValue: Option[T] = None): Boolean = {
    canHelper[T](playerId, skill).fold(DEFAULT_SKILL_RESPONSE)(t => t._1.can(t._2, expectedValue))
  }

  def reset(playerId: UUID, skill: Skill[_]): List[Skill[_]] = {
    Skill().get(skill.name)
      .asInstanceOf[Option[Skill[_]]]
      .map(upsert(playerId, _))
      .getOrElse(List.empty)
  }

  def reset(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = reset(player.getUUID, skill)

  def calculateValue[T](player: MinePlayer, skill: Skill[T], value: Option[T]): Option[Skill[T]] = {
    get[T](player, skill.name)
      .orElse(Skill().get[T](skill.name))
      .filter(_.areChangesAllowed())
      .filter(_.isAllowedValue(value))
      .filter(_.value != value)
      .map(_.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(value))
  }
}
