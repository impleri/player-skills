package net.impleri.playerskills.server.skills

import cats.implicits.toFoldableOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.server.skills.storage.PersistentStorage
import net.impleri.playerskills.server.skills.storage.SkillFileMissing
import net.impleri.playerskills.server.skills.storage.SkillNbtStorage
import net.impleri.playerskills.server.skills.storage.SkillResourceFile
import net.impleri.playerskills.utils.PlayerSkillsLogger

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/**
 * Public wrapper to rest of storage package
 */
case class PlayerStorageIO private[skills] (
  private val storage: PersistentStorage,
  private[skills] val skillFile: SkillResourceFile,
  private val skillTypeOps: SkillTypeOps,
  private val logger: PlayerSkillsLogger,
) {
  def read(playerId: UUID): List[Skill[_]] = {
    skillFile.getPlayerFile(playerId)
      .tap(logger.debugP(file => s"Reading from file ${file.getPath}"))
      .pipe(storage.read)
      .tap {
        case Right(_) => logger.debug(s"Restoring saved skills for $playerId")
        case Left(SkillFileMissing(_)) => ()
        case Left(e) => logger.warn(e.toString)
      }
      .map(skillTypeOps.deserializeAll)
      .toList
      .flatten
  }

  def write(playerId: UUID, skills: List[Skill[_]]): Boolean = {
    val skillsAsString = skills.flatMap(skillTypeOps.serialize(_))

    skillFile.getPlayerFile(playerId)
      .tap(logger.debugP(file => s"Writing to file ${file.getPath}"))
      .pipe(storage.write(_, skillsAsString))
      .tap {
        case Right(_) => logger.info(s"Saving skills for $playerId")
        case Left(e) => logger.warn(e.toString)
      }
      .getOrElse(false)
  }
}

object PlayerStorageIO {
  private[skills] def apply(
    resourceFile: SkillResourceFile,
    storage: PersistentStorage,
    skillTypeOps: SkillTypeOps,
    logger: PlayerSkillsLogger,
  ): PlayerStorageIO = {
    new PlayerStorageIO(storage, resourceFile, skillTypeOps, logger)
  }

  protected[server] def apply(
    server: Server,
    storage: PersistentStorage = SkillNbtStorage(),
    skillTypeOps: SkillTypeOps = SkillType(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): PlayerStorageIO = {
    SkillResourceFile(server).pipe(apply(_, storage, skillTypeOps, logger))
  }
}
