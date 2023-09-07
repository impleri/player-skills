package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * Orchestration for writing serialized strings into storage
 */
object SkillStorage {
  private val storage: PersistentStorage = SkillNbtStorage()
  fun setup(server: MinecraftServer) {
    SkillResourceFile.createInstance(server)
  }

  fun write(playerUuid: UUID, skills: List<String>) {
    val file = SkillResourceFile.forPlayer(playerUuid)
    PlayerSkillsLogger.SKILLS.debug("Writing to ${file.path}")
    storage.write(file, skills)
  }

  fun read(playerUuid: UUID): List<String> {
    val file = SkillResourceFile.forPlayer(playerUuid)
    PlayerSkillsLogger.SKILLS.debug("Reading file ${file.path}")
    val skills = storage.read(file)

    // write skill list back to file
    write(playerUuid, skills)
    return skills
  }
}
