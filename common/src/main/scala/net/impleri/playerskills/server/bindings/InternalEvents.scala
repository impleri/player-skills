package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.server.EventHandler
import net.impleri.playerskills.server.PlayerSkillsServer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

//trait BlockSync {
//    private var playerMap: mutable.HashMap[ServerPlayer, Long] = mutable.HashMap()
////  private def onJoin(player: ServerPlayer): Unit = playerMap.update(player, BlockRestrictions.getReplacementsCountFor(player))
////  private def onQuit(player: ServerPlayer) = playerMap.remove(player)
//
//  protected def checkForBlockUpdates(player: ServerPlayer, prev: Long): Boolean = {
//    val next = BlockRestrictions.getReplacementsCountFor(prev)
//
//    // We're assuming that the number of replaced blocks should change if a skill change actually changes replacements
//    // If we run into an issue where a skills change should trigger a refresh but the count difference doesn't change,
//    // we'll have to rework this
//    if (prev != next) {
//      playerMap.update(player, next)
//      true
//    } else false
//      }
//
//  protected def maybeUpdateBlocks(playerId: UUID): Unit = {
//    playerMap.find(_._1.getUUID == playerId)
//      .flatMap(t => if (checkForBlockUpdates(t._1, t._2)) Some(t._1) else None)
//      .foreach(p => p.getLevel.chunkSource.chunkMap.updatePlayerStatus(p, true))
//  }
//}

case class InternalEvents(
  eventHandler: EventHandler,
  onReload: ResourceManager => Unit,
  reloadListeners: ReloadListeners = ReloadListeners(),
)
  extends ResourceManagerReloadListener {
  private[server] def registerEvents(): Unit = {
    // Player Skills Events
    eventHandler.onSkillChanged(onSkillChanged)

    //    PlayerEvent.PLAYER_JOIN.register(onJoin _)
    //    PlayerEvent.PLAYER_QUIT.register(onQuit _ )

    // Vanilla Events
    reloadListeners.register(this)
    reloadListeners.register(SkillsDataLoader())
  }

  override def onResourceManagerReload(resourceManager: ResourceManager): Unit = onReload(resourceManager)

  private def onSkillChanged(event: SkillChangedEvent[_]): Unit = {
    PlayerSkillsServer.STATE.getNetHandler.syncPlayer(event)
    //    maybeUpdateBlocks(event.player.getUUID)
  }
}
