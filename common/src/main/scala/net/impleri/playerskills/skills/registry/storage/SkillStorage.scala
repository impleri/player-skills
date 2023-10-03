package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.MinecraftServer

import java.io.File
import java.util.UUID

class SkillStorage(private val storage: PersistentStorage) {
  private def readFile(file: File): Either[NbtFileReadError, List[String]] = {
    PlayerSkillsLogger.SKILLS.debug(s"Reading from file ${file.getPath}")
    storage.read(file)
  }

  def read(playerId: UUID): Either[NbtFileReadError, List[String]] =
    SkillResourceFile.forPlayer(playerId)
      .toRight(ReadBeforeServerLoaded())
      .flatMap(readFile)

  private def writeToFile(skills: List[String])(file: File): Either[NbtFileWriteError, Boolean] = {
    PlayerSkillsLogger.SKILLS.debug(s"Writing to file ${file.getPath}")
    storage.write(file, skills)
  }

  def write(playerId: UUID, skills: List[String]): Either[NbtFileWriteError, Boolean] =
    SkillResourceFile.forPlayer(playerId)
      .toRight(WriteBeforeServerLoaded())
      .flatMap(writeToFile(skills))
}

object SkillStorage {
  private lazy val instance: SkillStorage = apply()

  def apply(storage: PersistentStorage = SkillNbtStorage()): SkillStorage = new SkillStorage(storage)

  def setup(server: MinecraftServer): Unit = SkillResourceFile.createInstance(server)

  def cleanup(): Unit = SkillResourceFile.destroyInstance()

  def read(playerId: UUID): Either[NbtFileReadError, List[String]] = instance.read(playerId)

  def write(playerId: UUID, skills: List[String]): Either[NbtFileWriteError, Boolean] = instance.write(playerId, skills)
}
