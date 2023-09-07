package net.impleri.playerskills.server

import net.impleri.playerskills.api.Player
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.network.SyncSkillsMessage
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.level.ServerPlayer

object NetHandler {
  fun syncPlayer(event: SkillChangedEvent<*>) {
    val player = event.player
    if (player is ServerPlayer) {
      syncPlayer(player, false)
    } else {
      PlayerSkillsLogger.SKILLS.warn("Attempted to sync skill changes from clientside")
    }
  }

  @JvmOverloads
  fun syncPlayer(player: ServerPlayer?, force: Boolean = true) {
    if (player == null) {
      PlayerSkillsLogger.SKILLS.warn("Attempted to sync skill changes for nobody")
      return
    }
    val skills = Player.get(player).toMutableList()
    PlayerSkillsLogger.SKILLS.debug("Syncing ${skills.size} player skills to ${player.name}")
    SyncSkillsMessage(player, skills, force).sendTo(player)
  }
}
