package net.impleri.playerskills.network

import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.simple.SimpleNetworkManager
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.utils.PlayerSkillsLogger

object Manager {
  private val NET: SimpleNetworkManager = SimpleNetworkManager.create(PlayerSkills.MOD_ID)

  val SYNC_SKILLS: MessageType = NET.registerS2C("sync_skills") { SyncSkillsMessage(it) }

  val RESYNC_SKILLS: MessageType = NET.registerC2S("resync_skills") { ResyncSkillsMessage(it) }

  fun register() {
    PlayerSkillsLogger.SKILLS.debug("Registered network channels")
  }
}
