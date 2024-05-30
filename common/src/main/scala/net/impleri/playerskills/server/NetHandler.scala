package net.impleri.playerskills.server

import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.network.SyncSkillsMessageFactory
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.{Player => MinecraftPlayer}
import net.impleri.slab.logging.Logger

import scala.util.chaining.scalaUtilChainingOps

class NetHandler(
  private val playerOps: Player,
  private val messageFactory: SyncSkillsMessageFactory,
  private val logger: Logger,
) {
  def syncPlayer(player: MinecraftPlayer[_], force: Boolean = true): Unit = {
    playerOps.get(player)
      .tap(logger.debugP(s => s"Syncing ${s.size} player skills to ${player.name}"))
      .pipe(messageFactory.send(player, _, force))
      .foreach(player.sendMessage)
  }

  def syncPlayer(event: SkillChangedEvent[_]): Unit = {
    if (!event.player.isServer) {
      logger.warn("Attempted to sync skill changes from clientside")
    } else {
      syncPlayer(event.player, force = false)
    }
  }
}

object NetHandler {
  def apply(
    playerOps: Player = Player(),
    messageFactory: SyncSkillsMessageFactory = SyncSkillsMessageFactory(),
    logger: Logger = PlayerSkillsLogger.SKILLS,
  ): NetHandler = {
    new NetHandler(
      playerOps,
      messageFactory,
      logger,
    )
  }
}
