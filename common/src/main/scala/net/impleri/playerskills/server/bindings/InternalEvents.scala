package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.server.EventHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.data.restrictions.ItemRestrictionDataLoader
import net.impleri.playerskills.data.restrictions.RecipeRestrictionDataLoader
import net.impleri.slab.resources.ReloadListeners
import net.impleri.slab.resources.ResourceManager
import net.impleri.slab.resources.SimpleReloadListener

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
  itemRestrictionBuilder: ItemRestrictionDataLoader,
  recipeRestrictionBuilder: RecipeRestrictionDataLoader,
  eventHandler: EventHandler = EventHandler(),
  globalState: StateContainer = StateContainer(),
  serverStateContainer: ServerStateContainer = ServerStateContainer(),
  onReloadFn: Option[ResourceManager] => Unit = _ => {},
  reloadListeners: ReloadListeners = ReloadListeners(),
) extends SimpleReloadListener {
  private[server] def registerEvents(): Unit = {
    // Player Skills Events
    eventHandler.onSkillChanged(onSkillChanged)

    // Vanilla Events
    reloadListeners.registerServer(this)
    reloadListeners.registerServer(SkillsDataLoader(globalState.SKILL_OPS))
    reloadListeners.registerServer(itemRestrictionBuilder)
    reloadListeners.registerServer(recipeRestrictionBuilder)
  }

  private[bindings] def onSkillChanged(event: SkillChangedEvent[_]): Unit = {
    serverStateContainer.getNetHandler.syncPlayer(event)
    //    maybeUpdateBlocks(event.player.getUUID)
  }

  override protected[bindings] def onReload(
    manager: Option[ResourceManager],
  ): Unit =
    onReloadFn(manager)
}
