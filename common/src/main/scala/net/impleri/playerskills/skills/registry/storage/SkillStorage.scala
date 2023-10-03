package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.MinecraftServer

import java.io.File
import java.util.UUID
import scala.util.chaining._

class SkillStorage(private val storage: PersistentStorage, private val skillFile: SkillResourceFile) {
  private def readFile(file: File): Either[NbtFileReadError, List[String]] = {
    PlayerSkillsLogger.SKILLS.debug(s"Reading from file ${file.getPath}")
    storage.read(file)
  }

  def read(playerId: UUID): Either[NbtFileReadError, List[String]] =
    skillFile.getPlayerFile(playerId)
      .pipe(readFile)

  private def writeToFile(skills: List[String])(file: File): Either[NbtFileWriteError, Boolean] = {
    PlayerSkillsLogger.SKILLS.debug(s"Writing to file ${file.getPath}")
    storage.write(file, skills)
  }

  def write(playerId: UUID, skills: List[String]): Either[NbtFileWriteError, Boolean] =
    skillFile.getPlayerFile(playerId)
      .pipe(writeToFile(skills))
}

object SkillStorage {
  private[storage] def apply(resourceFile: SkillResourceFile, storage: PersistentStorage = SkillNbtStorage()): SkillStorage = new SkillStorage(storage, resourceFile)

  def apply(server: MinecraftServer): SkillStorage = SkillResourceFile(server)
    .pipe(apply(_))
}
