package net.impleri.playerskills.events.handlers

import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.server.NetHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

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

case class InternalEvents(onReload: ResourceManager => Unit) extends ResourceManagerReloadListener {
  private[handlers] def registerEvents(): Unit = {
    // Player Skills Events
    SkillChangedEvent.EVENT.register(onSkillChanged _)

    //    PlayerEvent.PLAYER_JOIN.register(onJoin _)
    //    PlayerEvent.PLAYER_QUIT.register(onQuit _ )

    // Vanilla Events
    ReloadListenerRegistry.register(PackType.SERVER_DATA, this)
    ReloadListenerRegistry.register(PackType.SERVER_DATA, SkillsDataLoader())
  }

  override def onResourceManagerReload(resourceManager: ResourceManager): Unit = onReload(resourceManager)

  def resyncPlayer(server: MinecraftServer, playerId: UUID): Unit = {
    server.getPlayerList.getPlayer(playerId)
      .tap(NetHandler.syncPlayer(_))
  }

  private def onSkillChanged(event: SkillChangedEvent[_]): Unit = {
    NetHandler.syncPlayer(event)
    //    maybeUpdateBlocks(event.player.getUUID)
  }
}
