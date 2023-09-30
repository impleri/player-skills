package net.impleri.playerskills.server

import net.impleri.playerskills.api.skills.Player
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.network.SyncSkillsMessage
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.level.ServerPlayer

import scala.util.chaining._

object NetHandler {
  def syncPlayer(player: ServerPlayer, force: Boolean = true): Unit =
    Player.get(player)
      .tap(s => PlayerSkillsLogger.SKILLS.debug(s"Syncing ${s.size} player skills to ${player.getName}"))
      .pipe(SyncSkillsMessage(player, _, force))

  def syncPlayer(event: SkillChangedEvent[_]): Unit =
    event.player match {
      case p: ServerPlayer => syncPlayer(p, force = false)
      case _ => PlayerSkillsLogger.SKILLS.warn("Attempted to sync skill changes from clientside")
    }
}
