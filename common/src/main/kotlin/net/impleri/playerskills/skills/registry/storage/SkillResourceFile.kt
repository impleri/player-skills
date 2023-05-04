package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.PlayerSkills
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.io.File
import java.nio.file.Path
import java.util.UUID

/**
 * Manages _where_ to save data
 */
internal class SkillResourceFile private constructor(private val storage: Path) {
  private fun getPlayerFile(playerUuid: UUID): File {
    return File(playerDirectory, "$playerUuid.skills")
  }

  private val playerDirectory: File
    get() {
      val playerDir = File(storageDirectory, "players")
      ensureDirectory(playerDir)
      return playerDir
    }
  private val storageDirectory: File
    get() {
      val dataDir = storage.toFile()
      ensureDirectory(dataDir)
      return dataDir
    }

  companion object {
    private var instance: SkillResourceFile? = null
    fun createInstance(server: MinecraftServer) {
      val levelResource = LevelResource(PlayerSkills.MOD_ID)
      val gameFolder = server.getWorldPath(levelResource)
      instance = SkillResourceFile(gameFolder)
    }

    fun destroyInstance() {
      instance = null
    }

    fun forPlayer(playerUuid: UUID): File {
      return instance?.getPlayerFile(playerUuid)
             ?: throw RuntimeException("Accessing file when the server is not running")
    }

    private fun ensureDirectory(file: File) {
      file.mkdirs()
    }
  }
}
