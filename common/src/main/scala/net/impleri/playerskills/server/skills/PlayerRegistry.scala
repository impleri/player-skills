package net.impleri.playerskills.server.skills

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.StatefulRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.VisibleForTesting

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/**
 * Orchestrated handling of player skills
 */
case class PlayerRegistry(
  override var state: PlayerRegistryState.CachedPlayers,
  private[skills] val storage: Option[PlayerStorageIO],
  private val skillsRegistry: SkillRegistry,
  private val logger: PlayerSkillsLogger,
) extends StatefulRegistry[PlayerRegistryState.CachedPlayers] {
  if (storage.isEmpty) logger.warn("Player registry opened without server") else logger.debug("Opened player registry")

  def entries: List[(UUID, List[Skill[_]])] = PlayerRegistryState.entries().pipe(maintainState)

  private def save(playerId: UUID, skills: List[Skill[_]]): Boolean = {
    PlayerRegistryState.upsert(playerId, skills).pipe(maintainState)
    storage.forall(_.write(playerId, skills))
  }

  private def openFor(playerId: UUID) =
    storage.map(_.read(playerId))
      .map(PlayerRegistry.filterRegisteredSkills(skillsRegistry.entries, _))
      .map(s => s ++ PlayerRegistry.ensureRegisteredSkills(skillsRegistry.entries, s))
      .tap(_.foreach(save(playerId, _)))
      .toList
      .flatten

  def open(playerIds: List[UUID]): List[UUID] =
    playerIds
      .filterNot(PlayerRegistryState.has(_).pipe(maintainState))
      .map(p => (p, openFor(p)))
      .toMap
      .tap(PlayerRegistryState.upsertMany(_).pipe(maintainState))
      .keys
      .toList

  def open(playerId: UUID): List[Skill[_]] = {
    open(List(playerId))
      .headOption
      .foreach(PlayerRegistryState.get(_).pipe(maintainState))

    get(playerId)
  }

  def get(playerId: UUID): List[Skill[_]] =
    PlayerRegistryState
      .get(playerId)
      .pipe(maintainState)

  def has(playerId: UUID): Boolean =
    PlayerRegistryState
      .has(playerId)
      .pipe(maintainState)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .filterNot(_.name == skill.name)
      .tap(ss => if (ss.nonEmpty) logger.info(s"Replacing ${skill.name} for $playerId"))
      .pipe(_ ++ List(skill))
      .tap(save(playerId, _))


  def addSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .pipe(PlayerRegistry.safeAdd(skill))
      .tap(save(playerId, _))

  def removeSkill(playerId: UUID, name: ResourceLocation): List[Skill[_]] =
    get(playerId)
      .pipe(_.filterNot(_.name == name))
      .tap(save(playerId, _))

  def removeSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] = removeSkill(playerId, skill.name)

  private def closeFor(playerId: UUID) = {
    logger.info(s"Closing player $playerId, ensuring skills are saved")
    get(playerId)
      .pipe(s => storage.map(_.write(playerId, s)))
  }

  def close(playerIds: List[UUID]): List[UUID] =
    playerIds
      .map(p => (p, closeFor(p)))
      .toMap
      .partition(_._2.forall(_ == true))
      .tap(p => if (p._1.nonEmpty) logger.warn(s"Could not save player data for: ${p._1.keys.mkString(",")}"))
      ._2
      .keys
      .toList
      .tap(PlayerRegistryState.removeMany(_).pipe(maintainState))

  def close(playerId: UUID): Boolean =
    close(List(playerId))
      .contains(playerId)

  def close(): List[UUID] =
    PlayerRegistryState.entries()
      .pipe(maintainState)
      .map(_._1)
      .pipe(close)
      .tap(_ => state = PlayerRegistryState.empty)
}

object PlayerRegistry {
  private def filterRegisteredSkills(source: List[Skill[_]], target: List[Skill[_]]) =
    source
      .map(_.name)
      .pipe(s => target.filter(t => s.contains(t.name)))

  private def ensureRegisteredSkills(source: List[Skill[_]], target: List[Skill[_]]) =
    target
      .map(_.name)
      .pipe(t => source.filterNot(s => t.contains(s.name)))

  private def safeAdd(skill: Skill[_])(skills: List[Skill[_]]): List[Skill[_]] =
    if (skills.exists(_.name == skill.name)) skills else skills ++ List(skill)

  @VisibleForTesting
  private[skills] def apply(
    storage: PlayerStorageIO,
    state: PlayerRegistryState.CachedPlayers,
    skillsRegistry: SkillRegistry,
    logger: PlayerSkillsLogger,
  ) = new PlayerRegistry(state, Some(storage), skillsRegistry, logger)

  def apply(
    server: Option[MinecraftServer] = None,
    state: PlayerRegistryState.CachedPlayers = PlayerRegistryState.empty,
    skillsRegistry: SkillRegistry = StateContainer.SKILLS,
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): PlayerRegistry = new PlayerRegistry(state, server.map(PlayerStorageIO(_)), skillsRegistry, logger)
}
