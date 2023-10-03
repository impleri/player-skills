package net.impleri.playerskills.skills.registry

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.skills.registry.storage.SkillFileMissing
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import java.util.UUID
import scala.collection.mutable
import scala.util.chaining._

/**
 * Handles the cache side of interacting with player skills
 */
sealed trait CachedPlayers {
  private val players: mutable.Map[UUID, List[Skill[_]]] = mutable.HashMap()

  private[playerskills] def init(): Unit = ()

  def entries(): Map[UUID, List[Skill[_]]] = Map.from(players)

  def has(playerId: UUID): Boolean = players.contains(playerId)

  def readFromCache(playerId: UUID): Option[List[Skill[_]]] = players.get(playerId)

  protected def writeToCache(playerId: UUID, skills: List[Skill[_]]): Unit = players.update(playerId, skills)

  protected def writeBulkCache(values: Map[UUID, List[Skill[_]]]): List[UUID] =
    values
      .tap(players.addAll)
      .keys
      .toList

  protected def removeFromCache(playerId: UUID): Option[List[Skill[_]]] = players.remove(playerId)

  protected def removeBulkCache(playerIds: List[UUID]): Unit = playerIds.foreach(removeFromCache)

  protected def clearCache(): Unit = players.clear()
}

/**
 * Handles the I/O side of interacting with player skills
 */
trait PersistedPlayers {
  protected def storage: () => SkillStorage
  protected def readFromStorage(playerId: UUID): Option[List[Skill[_]]] =
    storage().read(playerId)
      .tap {
        case Right(_) => PlayerSkillsLogger.SKILLS.debug(s"Restoring saved skills for $playerId")
        case Left(SkillFileMissing(_)) => ()
        case Left(e) => PlayerSkillsLogger.SKILLS.warn(e.toString)
      }
      .map(SkillType.deserializeAll)
      .toOption

  private def serializeSkills(skills: List[Skill[_]]): List[String] = skills.flatMap(SkillType.serialize(_))

  protected def writeToStorage(playerId: UUID, skills: List[Skill[_]]): Boolean =
    storage().write(playerId, serializeSkills(skills))
      .tap {
        case Right(_) => PlayerSkillsLogger.SKILLS.info(s"Saving skills for $playerId")
        case Left(e) => PlayerSkillsLogger.SKILLS.warn(e.toString)
      }
      .toOption
      .getOrElse(false)
}

/**
 * Orchestrated handling of player skills
 */
object Players extends CachedPlayers with PersistedPlayers {
  override def storage: () => SkillStorage = () => EventHandlers.withServer.pipe(SkillStorage.apply)
  private def save(playerId: UUID, skills: List[Skill[_]]): Boolean = {
    writeToCache(playerId, skills)
    writeToStorage(playerId, skills)
  }

  private def filterRegisteredSkills(source: List[Skill[_]], target: List[Skill[_]]) =
    source
      .map(_.name)
      .pipe(s => target.filter(t => s.contains(t.name)))

  private def ensureRegisteredSkills(source: List[Skill[_]], target: List[Skill[_]]) =
    target
      .map(_.name)
      .pipe(t => source.filterNot(s => t.contains(s.name)))

  private def openFor(playerId: UUID) =
    readFromStorage(playerId)
      .getOrElse(List.empty)
      .pipe(filterRegisteredSkills(Skills.entries, _))
      .pipe(s =>  s ++ ensureRegisteredSkills(Skills.entries, s))
      .tap(save(playerId, _))

  def open(playerIds: List[UUID]): List[UUID] =
    playerIds
      .filterNot(has)
      .map(p => (p, openFor(p)))
      .toMap
      .pipe(writeBulkCache)

  def open(playerId: UUID): List[Skill[_]] =
    open(List(playerId))
      .headOption
      .flatMap(readFromCache)
      .getOrElse(List.empty)

  def get(playerId: UUID): List[Skill[_]] =
    readFromCache(playerId)
      .getOrElse(open(playerId))

  def upsert(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .filterNot(_.name == skill.name)
      .tap(ss => if (ss.nonEmpty) PlayerSkillsLogger.SKILLS.info(s"Replacing ${skill.name} for $playerId"))
      .pipe(_ ++ List(skill))
      .tap(writeToStorage(playerId, _))

  private def safeAdd(skill: Skill[_])(skills: List[Skill[_]]): List[Skill[_]] =
    if (skills.exists(_.name == skill.name)) skills else skills ++ List(skill)

  def addSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] =
    get(playerId)
      .pipe(safeAdd(skill))
      .tap(save(playerId, _))

  def removeSkill(playerId: UUID, name: ResourceLocation): List[Skill[_]] =
    get(playerId)
      .pipe(_.filterNot(_.name == name))
      .tap(save(playerId, _))

  def removeSkill(playerId: UUID, skill: Skill[_]): List[Skill[_]] = removeSkill(playerId, skill.name)

  private def closeFor(playerId: UUID) =
    playerId
      .tap(p => PlayerSkillsLogger.SKILLS.info(s"Closing player $p, ensuring skills are saved"))
      .pipe(get)
      .pipe(writeToStorage(playerId, _))

  def close(playerIds: List[UUID]): List[UUID] =
    playerIds
      .map(p => (p, closeFor(p)))
      .toMap
      .partition(_._2 == false)
      .tap(p => if(p._1.nonEmpty) PlayerSkillsLogger.SKILLS.warn(s"Could not save player data for: ${p._1.keys.mkString(",")}"))
      ._2
      .keys
      .toList
      .tap(removeBulkCache)

    def close(playerId: UUID): Boolean =
      close(List(playerId))
        .contains(playerId)


  def close(): List[UUID] =
    entries()
      .pipe(_.keys.toList)
      .pipe(close)
      .tap(_ => clearCache())
}
