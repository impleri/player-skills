package net.impleri.playerskills.server.skills

import cats.data.State
import net.impleri.playerskills.api.skills.Skill

import java.util.UUID

/**
 * Handles the cache side of interacting with player skills
 */
object PlayerRegistryState {
  final case class CachedPlayers private(protected val state: Map[UUID, List[Skill[_]]]) {
    def entries(): List[(UUID, List[Skill[_]])] = state.toList

    def has(playerId: UUID): Boolean = state.contains(playerId)

    def get(playerId: UUID): List[Skill[_]] = state.get(playerId).toList.flatten

    def upsert(
      playerId: UUID,
      skills: List[Skill[_]]
    ): CachedPlayers = CachedPlayers(remove(playerId).state + (playerId -> skills))

    def upsertMany(values: Map[UUID, List[Skill[_]]]): CachedPlayers = CachedPlayers(removeMany(values.keys.toList).state ++ values)

    def remove(playerId: UUID): CachedPlayers = CachedPlayers(state.filterNot(_._1 == playerId))

    def removeMany(playerIds: List[UUID]): CachedPlayers = CachedPlayers(state.filterNot(
      e => playerIds.contains(e._1)))
  }

  private def readOp[T](f: CachedPlayers => T): State[CachedPlayers, T] = State[CachedPlayers, T](s => (s, f(s)))

  val empty: CachedPlayers = CachedPlayers(Map.empty)

  def entries(): State[CachedPlayers, List[(UUID, List[Skill[_]])]] = readOp(_.entries())

  def has(key: UUID): State[CachedPlayers, Boolean] = readOp(_.has(key))

  def get(key: UUID): State[CachedPlayers, List[Skill[_]]] = readOp(_.get(key))

  def upsert(key: UUID, skills: List[Skill[_]]): State[CachedPlayers, Unit] = State.modify(_.upsert(key, skills))

  def upsertMany(values: Map[UUID, List[Skill[_]]]): State[CachedPlayers, Unit] = State.modify(_.upsertMany(values))

  def remove(key: UUID): State[CachedPlayers, Unit] = State.modify(_.remove(key))

  def removeMany(keys: List[UUID]): State[CachedPlayers, Unit] = State.modify(_.removeMany(keys))
}
