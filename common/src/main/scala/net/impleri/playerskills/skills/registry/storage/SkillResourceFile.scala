package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.PlayerSkills
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import org.jetbrains.annotations.VisibleForTesting

import java.io.File
import java.nio.file.Path
import java.util.UUID
import scala.util.chaining._

/**
 * Manages _where_ to save data
 */
class SkillResourceFile private[storage] (private[storage] val storage: Path) {
  private def storageDirectory: File =
    storage.toFile
      .tap(_.mkdirs())

  private def playerDirectory: File =
    storageDirectory
      .pipe(new File(_, "players"))
      .tap(_.mkdirs())

  private def getPlayerFile(playerId: UUID): File =
    playerDirectory
      .pipe(new File(_, s"$playerId.skills"))
}

object SkillResourceFile {
  @VisibleForTesting
  private[storage] var instance: Option[SkillResourceFile] = None

  private def apply(storage: Path): SkillResourceFile = new SkillResourceFile(storage)

  protected[storage] def createInstance(server: MinecraftServer): Unit =
    instance = new LevelResource(PlayerSkills.MOD_ID)
      .pipe(server.getWorldPath)
      .pipe(apply)
      .pipe(Option(_))

  protected[storage] def destroyInstance(): Unit = instance = None

  protected[storage] def forPlayer(playerId: UUID): Option[File] =
    instance
      .map(_.getPlayerFile(playerId))
}
