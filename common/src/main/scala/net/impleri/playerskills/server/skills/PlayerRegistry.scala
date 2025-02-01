package net.impleri.playerskills.server.skills

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.StatefulRegistry
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/** Orchestrated handling of player skills
  */
case class PlayerRegistry(
  var state: PlayerRegistryState.CachedPlayers,
  private[server] var storage: Option[PlayerStorageIO],
  private val skillsRegistry: SkillRegistry,
  private val logger: Logger,
) extends StatefulRegistry[PlayerRegistryState.CachedPlayers] {
  def changeStorage(next: Option[PlayerStorageIO]): Unit =
    storage = next

  def entries: List[(UUID, List[Skill[_]])] =
    PlayerRegistryState.entries().pipe(maintainState)

  private def save(playerId: UUID)(skills: List[Skill[_]]): Boolean = {
    PlayerRegistryState
      .upsert(playerId, skills)
      .pipe(maintainState)
    storage.forall(_.write(playerId, skills))
  }

  private def openFor(playerId: UUID) =
    storage
      .map(_.read(playerId))
      .map(PlayerRegistry.filterRegisteredSkills(skillsRegistry.entries))
      .map(PlayerRegistry.ensureRegisteredSkills(skillsRegistry.entries))
      .map(_.distinctBy(_.name))
      .tap(_.foreach(save(playerId)))
      .toList
      .flatten

  def open(playerIds: List[UUID]): List[UUID] =
    playerIds
      .filter(_ => storage.nonEmpty)
      .filterNot(PlayerRegistryState.has(_).pipe(maintainState))
      .map(p => (p, openFor(p)))
      .toMap
      .tap(PlayerRegistryState.upsertMany(_).pipe(maintainState))
      .keys
      .toList

  def open(playerId: UUID): List[Skill[_]] = {
    playerId
      .pipe(List(_))
      .pipe(open)
      .headOption
      .foreach(
        PlayerRegistryState
          .get(_)
          .pipe(maintainState)
      )

    get(playerId)
  }

  def get(playerId: UUID): List[Skill[_]] = {
    if (!has(playerId)) {
      open(playerId)
    }

    PlayerRegistryState
      .get(playerId)
      .pipe(maintainState)
  }

  def has(playerId: UUID): Boolean =
    PlayerRegistryState
      .has(playerId)
      .pipe(maintainState)

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .filterNot(_.name == skill.name)
      .tap(ss =>
        if (ss.nonEmpty) logger.info(s"Replacing ${skill.name} for $playerId"),
      )
      .pipe(_ ++ List(skill))
      .tap(save(playerId))

  def addSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .pipe(PlayerRegistry.safeAdd(skill))
      .tap(save(playerId))

  def removeSkill(playerId: UUID, name: ResourceLocation): List[Skill[_]] =
    get(playerId)
      .pipe(_.filterNot(_.name == name))
      .tap(save(playerId))

  def removeSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    removeSkill(playerId, skill.name)

  private def closeFor(playerId: UUID) =
    playerId
      .tap(logger.infoP(p => s"Closing player $p, ensuring skills are saved"))
      .pipe(get)
      .pipe(s => storage.map(_.write(playerId, s)))

  def close(playerIds: List[UUID]): List[UUID] =
    playerIds
      .map(p => (p, closeFor(p)))
      .toMap
      .partition(_._2.contains(true))
      .tap(p =>
        if (p._2.nonEmpty)
          logger.warn(
            s"Could not save player data for: ${p._2.keys.mkString(",")}",
          ),
      )
      ._2
      .keys
      .toList
      .tap(PlayerRegistryState.removeMany(_).pipe(maintainState))

  def close(playerId: UUID): Boolean =
    close(List(playerId))
      .contains(playerId)

  def close(): List[UUID] =
    PlayerRegistryState
      .entries()
      .pipe(maintainState)
      .map(_._1)
      .pipe(close)
      .tap(_ => state = PlayerRegistryState.empty)
}

object PlayerRegistry {
  private def filterRegisteredSkills(source: List[Skill[_]])(
    target: List[Skill[_]],
  ) =
    source
      .map(_.name)
      .pipe(s => target.filter(t => s.contains(t.name)))

  private def ensureRegisteredSkills(source: List[Skill[_]])(
    target: List[Skill[_]],
  ) =
    target
      .map(_.name)
      .pipe(t => source.filterNot(s => t.contains(s.name)))
      .pipe(target ++ _)

  private def safeAdd(
    skill: Skill[_],
  )(skills: List[Skill[_]]): List[Skill[_]] =
    if (skills.exists(_.name == skill.name)) skills else skills ++ List(skill)

  def apply(
    storage: Option[PlayerStorageIO] = None,
    state: PlayerRegistryState.CachedPlayers = PlayerRegistryState.empty,
    skillsRegistry: SkillRegistry = SkillRegistry(),
    logger: Logger = PlayerSkillsLogger.SKILLS,
  ): PlayerRegistry =
    new PlayerRegistry(state, storage, skillsRegistry, logger)
}
