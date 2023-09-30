package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.PlayerSkills
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource

import java.io.File
import java.nio.file.Path
import java.util.UUID
import scala.util.chaining._

/**
 * Manages _where_ to save data
 */
class SkillResourceFile private (private val storage: Path) {
  private def storageDirectory: File =
    storage
      .pipe(_.toFile)
      .tap(SkillResourceFile.ensureDirectory)

  private def playerDirectory: File =
    storageDirectory
      .pipe(new File(_, "players"))
      .tap(SkillResourceFile.ensureDirectory)

  private def getPlayerFile(playerId: UUID): File =
    playerDirectory
      .pipe(new File(_, s"$playerId.skills"))
}

object SkillResourceFile {
  private def ensureDirectory(file: File): Unit = file.mkdirs()

  private var instance: Option[SkillResourceFile] = None

  private def apply(storage: Path): SkillResourceFile = new SkillResourceFile(storage)

  def createInstance(server: MinecraftServer): Unit =
    instance = new LevelResource(PlayerSkills.MOD_ID)
      .pipe(server.getWorldPath)
      .pipe(apply)
      .pipe(Option(_))

  def destroyInstance(): Unit = instance = None

  def forPlayer(playerId: UUID): Option[File] =
    instance
      .map(_.getPlayerFile(playerId))
}
