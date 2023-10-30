package net.impleri.playerskills.server

import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.packs.resources.ResourceManager

import scala.annotation.unused

/**
 * Single place for all stateful classes
 */
object ServerStateContainer {
  var SERVER: Option[MinecraftServer] = None
  private var STORAGE: Option[PlayerStorageIO] = None
  var PLAYERS: PlayerRegistry = PlayerRegistry()
  var TEAM: Team = StubTeam()
  val NETWORK: NetHandler = NetHandler()

  val EVENT_HANDLERS: EventHandlers = EventHandlers(onServerChange = onServerChange, onReloadResources = onReload)

  def init(): Unit = PlayerSkillsLogger.SKILLS.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = TEAM = instance

  private def onServerChange(next: Option[MinecraftServer] = None): Unit = {
    SERVER = next
    STORAGE = next.map(PlayerStorageIO(_))
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState)
  }

  private def onReload(@unused resourceManager: ResourceManager): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers)
      .foreach(_.foreach(NETWORK.syncPlayer(_)))
  }
}
