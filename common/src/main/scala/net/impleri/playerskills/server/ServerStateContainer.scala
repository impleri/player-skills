package net.impleri.playerskills.server

import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.server.skills.PlayersRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.resources.ResourceManager

import scala.jdk.javaapi.CollectionConverters

/**
 * Single place for all stateful classes
 */
object ServerStateContainer {
  var server: Option[MinecraftServer] = None
  var storage: Option[PlayerStorageIO] = None
  var PLAYERS: PlayersRegistry = PlayersRegistry()

  val EVENT_HANDLERS: EventHandlers = EventHandlers(onServerChange = onServerChange, onReloadResources = onReload)

  def init(): Unit = PlayerSkillsLogger.SKILLS.info("PlayerSkills Server Loaded")

  private def onServerChange(next: Option[MinecraftServer] = None): Unit = {
    server = next
    storage = next.map(PlayerStorageIO(_))
    PLAYERS = PlayersRegistry(next, PLAYERS.getState)
  }

  private def onReload(resourceManager: ResourceManager): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    server.map(_.getPlayerList.getPlayers)
      .map(CollectionConverters.asScala(_).toList)
      .foreach(_.foreach(NetHandler.syncPlayer(_)))
  }
}
