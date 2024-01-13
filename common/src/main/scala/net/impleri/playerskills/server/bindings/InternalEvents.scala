package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.server.EventHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.data.restrictions.ItemRestrictionDataLoader
import net.impleri.playerskills.restrictions.item.ItemRestrictionBuilder
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
  itemRestrictionBuilder: ItemRestrictionBuilder,
  eventHandler: EventHandler = EventHandler(),
  globalState: StateContainer = StateContainer(),
  serverStateContainer: ServerStateContainer = ServerStateContainer(),
  onReload: ResourceManager => Unit = _ => {},
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
    reloadListeners.register(SkillsDataLoader(globalState.SKILL_OPS))
    reloadListeners.register(
      ItemRestrictionDataLoader(
        itemRestrictionBuilder,
        globalState.SKILL_OPS,
        globalState.SKILL_TYPE_OPS,
        serverStateContainer.PLAYER_OPS,
      ),
    )
  }

  override def onResourceManagerReload(resourceManager: ResourceManager): Unit = onReload(resourceManager)

  private[bindings] def onSkillChanged(event: SkillChangedEvent[_]): Unit = {
    serverStateContainer.getNetHandler.syncPlayer(event)
    //    maybeUpdateBlocks(event.player.getUUID)
  }
}
