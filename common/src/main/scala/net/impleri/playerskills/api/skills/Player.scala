package net.impleri.playerskills.api.skills

import net.impleri.playerskills.skills.registry.Players
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import java.util.UUID
import scala.util.chaining._

object Player {
  val DEFAULT_SKILL_RESPONSE: Boolean = true

  def get(playerId: UUID): List[Skill[_]] = Players.get(playerId)

  def get(player: MinePlayer): List[Skill[_]] = get(player.getUUID)

  def get[T](playerId: UUID, name: ResourceLocation): Option[Skill[T]] =
    Players.get(playerId)
      .find(_.name == name)
      .asInstanceOf[Option[Skill[T]]]

  def get[T](player: MinePlayer, name: ResourceLocation): Option[Skill[T]] =
    get(player.getUUID, name)

  def isOnline(playerId: UUID): Boolean = Players.has(playerId)

  def open(playerId: UUID): List[Skill[_]] = Players.open(playerId)

  def open(players: List[UUID]): List[UUID] = Players.open(players)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] = Players.upsert(playerId, skill)

  def upsert(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = upsert(player.getUUID, skill)

  def close(playerId: UUID): Boolean = Players.close(playerId)

  def close(players: List[UUID]): Boolean = Players.close(players).nonEmpty

  private def canHelper[T](playerId: UUID, skill: Skill[T]): Option[(SkillType[T], Skill[T])] =
    (SkillType.get(skill), get[T](playerId, skill.name)) match {
      case (Some(t), Some(s)) => Some((t, s))
      case _ => None
    }

  def can[T](playerId: UUID, skill: Skill[T], expectedValue: Option[T] = None): Boolean =
    canHelper[T](playerId, skill)
      .map(t => t._1.can(t._2, expectedValue))
      .getOrElse(DEFAULT_SKILL_RESPONSE)

  def reset(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    skill
      .pipe(_.name)
      .pipe(Skill.get)
      .map(upsert(playerId, _))
      .getOrElse(List.empty)

  def reset(player: MinePlayer, skill: Skill[_]): List[Skill[_]] = reset(player.getUUID, skill)

  def calculateValue[T](player: MinePlayer, skill: Skill[T], value: Option[T]): Option[Skill[T]] =
    get(player, skill.name)
      .orElse(Skill.get(skill.name))
      .asInstanceOf[Option[Skill[T]]]
      .filter(_.areChangesAllowed())
      .filter(_.isAllowedValue(value))
      .filter(_.value != value)
      .map(_.asInstanceOf[Skill[T] with ChangeableSkillOps[T, Skill[T]]].mutate(value))
}
