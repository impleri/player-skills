package net.impleri.playerskills.server.skills.storage

import net.impleri.playerskills.facades.minecraft.Server
import org.jetbrains.annotations.VisibleForTesting

import java.io.File
import java.nio.file.Path
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/**
 * Manages _where_ to save data
 */
class SkillResourceFile private[skills] (private[skills] val storage: Path) {
  private def storageDirectory: File = {
    storage.toFile
      .tap(_.mkdirs())
  }

  private def playerDirectory: File = {
    storageDirectory
      .pipe(new File(_, "players"))
      .tap(_.mkdirs())
  }

  // package-private as this should be accessed through SkillStorage
  private[skills] def getPlayerFile(playerId: UUID): File = {
    playerDirectory
      .pipe(new File(_, s"$playerId.skills"))
  }
}

object SkillResourceFile {
  @VisibleForTesting
  private[skills] def apply(storage: Path): SkillResourceFile = {
    new SkillResourceFile(storage)
  }

  protected[skills] def apply(server: Server): SkillResourceFile = {
    apply(server.getWorldPath())
  }
}
